package edu.utsa.fileflow.filestructure;

import java.util.HashMap;

import edu.utsa.fileflow.utilities.PrintDirectoryTree;

public class FileStruct implements Cloneable {

	private String name;
	public FileStruct parent;
	private final HashMap<String, FileStruct> files;

	/**
	 * This constructor will instantiate the new FileStruct object.
	 * 
	 * @param name
	 *            name of the directory
	 */
	public FileStruct(String name) {
		this.name = name;
		this.parent = null;
		this.files = new HashMap<String, FileStruct>();
	}

	/**
	 * Adds a file structure to the map of files.
	 * 
	 * @param fs
	 *            the file structure to add
	 */
	public FileStruct insert(FileStruct fs) {
		fs.parent = this;
		FileStruct node = files.put(fs.name, fs);
		return node;
	}

	/**
	 * Inserts the filePath into the file structure. If the directories do not exist, then it will create them.
	 * 
	 * @param filePath
	 *            the file path to insert into the file structure
	 * @return the lowest level FileStruct
	 */
	public FileStruct insert(FilePath filePath) {
		String[] tokens = filePath.getTokens();
		FileStruct next = this;
		FileStruct peek = this;
		// traverse through the file path until we find a directory that does not exist
		int i = 0;
		for (String token : tokens) {
			peek = peek.files.get(token);
			if (peek == null) {
				break;
			}
			next = peek;
			i += 1;
		}
		
		if (tokens.length > 0 && next.name.equals(tokens[tokens.length - 1])) {
			return next;
		} else {
			for (; i < tokens.length; i++) {
				next.insert(new FileStruct(tokens[i]));
				next = next.files.get(tokens[i]);
			}
		}
		
		return next;
	}

	public FileStruct insert(FileStruct fs, FilePath filePath) {
		FileStruct nodeToInsertAt = insert(filePath.getPathToFile());//insert(filePath.getPathToFile());
		fs.name = filePath.getFileName();
		return nodeToInsertAt.insert(fs);
	}

	/**
	 * Removes a FileStruct from its children
	 *
	 * @param fs
	 *            FileStruct to remove
	 */
	private FileStruct remove(FileStruct fs) {
		return files.remove(fs.name);
	}

	/**
	 * 
	 * @param filePath
	 *            path to the file to be removed
	 * @return the FileStruct that was removed
	 */
	public FileStruct remove(FilePath filePath) {
		FileStruct fileToRemove = getFileStruct(filePath);
		if (fileToRemove == null) {
			return null;
		}
		// FIXME: test if parent is null
		return fileToRemove.parent.remove(fileToRemove);
	}

	/**
	 * 
	 * @param filePath
	 *            path to the target FileStruct to be returned
	 * @return the FileStruct specified by the filePath
	 */
	public FileStruct getFileStruct(FilePath filePath) {
		String[] tokens = filePath.getTokens();
		FileStruct next = this;
		for (String token : tokens) {
			next = next.files.get(token);
			if (next == null)
				return null;
		}
		return next;
	}

	/**
	 * Checks if a file exists given the path to that file
	 * 
	 * @param filePath
	 * @return true if the file exists
	 */
	public boolean pathExists(FilePath filePath) {
		return getFileStruct(filePath) != null;
	}

	/*
	 * Getters and Setters for global class variables
	 */
	public String getName() {
		return name;
	}

	public HashMap<String, FileStruct> getFiles() {
		return files;
	}

	@Override
	public FileStruct clone() {
		FileStruct clone = new FileStruct(name);
		for (HashMap.Entry<String, FileStruct> entry : files.entrySet()) {
			clone.insert(entry.getValue().clone());
		}
		return clone;
	}

	@Override
	public String toString() {
		return PrintDirectoryTree.printDirectoryTree(this);
	}

	public boolean assertNotExists(FilePath arg1) {
		if(files.get(arg1) != null){
			System.out.println("file 1 exists");
			return false;
		}
		System.out.println("file 1 does not exist");
		return true;
	}

}
