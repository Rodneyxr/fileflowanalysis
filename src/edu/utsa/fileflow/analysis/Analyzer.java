package edu.utsa.fileflow.analysis;

import java.util.Stack;

import edu.utsa.fileflow.cfg.FlowPoint;
import edu.utsa.fileflow.cfg.FlowPointContext;
import edu.utsa.fileflow.cfg.FlowPointContextType;
import edu.utsa.fileflow.cfg.FlowPointEdge;

public class Analyzer<D extends AnalysisDomain, A extends Analysis<D>> {

	D domain;
	A analysis;

	public Analyzer(Class<D> d, Class<A> a) {
		try {
			// TODO: create factory interface for these
			domain = d.newInstance();
			analysis = a.newInstance();
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void analyze(FlowPoint cfg) {
		// initialize the workset
		Stack<FlowPoint> workset = new Stack<>();

		// add the start node to the workset
		updateAnalysis(cfg, domain);
		workset.add(cfg);

		while (!workset.isEmpty()) {
			FlowPoint flowpoint = workset.pop();

			// add all children to the workset
			for (FlowPointEdge edge : flowpoint.getOutgoingEdgeList()) {
				FlowPoint child = edge.getTarget();
				// for each outgoing edge, compute y (new domain)
				// then check if y is different from the old domain
				// if so, update domain and target to workset
				@SuppressWarnings("unchecked")
				AnalysisDomain y = updateAnalysis(child, (D) flowpoint.domain);
				if (y.compareTo(child.domain) != 0) {
					child.domain = y;
					workset.add(child);
				}
			}

		}
	}

	private AnalysisDomain updateAnalysis(FlowPoint target, D inputDomain) {
		AnalysisDomain result = null;
		FlowPointContext fpctx = target.getContext();
		FlowPointContextType type = fpctx.getType();

		// TODO: find an alternative to this
		if (target.domain == null) {
			target.domain = inputDomain.bottom();
		}

		analysis.onBefore(inputDomain, fpctx);

		switch (type) {
		case ProgEnter:
			result = analysis.enterProg(inputDomain, fpctx);
			break;
		case ProgExit:
			result = analysis.exitProg(inputDomain, fpctx);
		case FunctionCall:
			if (fpctx.getText().startsWith("touch")) {
				result = analysis.touch(inputDomain, fpctx);
			} else if (fpctx.getText().startsWith("mkdir")) {
				result = analysis.mkdir(inputDomain, fpctx);
			} else if (fpctx.getText().startsWith("rm")) {
				result = analysis.remove(inputDomain, fpctx);
			} else if (fpctx.getText().startsWith("copy")) {
				result = analysis.copy(inputDomain, fpctx);
			}
			break;
		case WhileStatement:
			result = analysis.enterWhileStatement(inputDomain, fpctx);
			break;
		case FlowPoint:
			if (fpctx.getText().equals("EXIT_WHILE")) {
				// TODO: make exitWhile enum
				result = analysis.exitWhileStatement(inputDomain, fpctx);
			}
			break;
		default:
			System.err.println("Not implemented: " + target);
			break;
		}

		analysis.onAfter(inputDomain, fpctx);

		return result;
	}

}
