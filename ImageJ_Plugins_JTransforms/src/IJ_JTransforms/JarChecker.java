package IJ_JTransforms;

import java.io.IOException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;

public class JarChecker {

	public JarChecker() {
	}

	protected boolean findJarRecursively(String dir, String jarName) {		
		boolean result = false;
		List<Path> fileList = new ArrayList<>();
		try {
			Files.walk(Paths.get(dir)).filter(Files::isRegularFile).forEach(fileList::add);
		} catch (IOException e) {
			e.printStackTrace();
		}
				
		for (int i = 0; i < fileList.size(); i++) {
			if (fileList.get(i).toString().contains(jarName)) {
				result = true;
				break;
			}
		}
		return result;
	}
}
