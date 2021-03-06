package the.bytecode.club.bytecodeviewer.resources.exporting.impl;

import the.bytecode.club.bytecodeviewer.BytecodeViewer;
import the.bytecode.club.bytecodeviewer.Configuration;
import the.bytecode.club.bytecodeviewer.gui.components.ExportJar;
import the.bytecode.club.bytecodeviewer.gui.components.FileChooser;
import the.bytecode.club.bytecodeviewer.resources.exporting.Exporter;
import the.bytecode.club.bytecodeviewer.util.DialogueUtils;

import javax.swing.*;
import java.io.File;

/**
 * @author Konloch
 * @since 6/27/2021
 */
public class RunnableJarExporter implements Exporter
{
	@Override
	public void promptForExport()
	{
		if (BytecodeViewer.promptIfNoLoadedClasses())
			return;
		
		Thread exportThread = new Thread(() ->
		{
			if (!BytecodeViewer.autoCompileSuccessful())
				return;
			
			JFileChooser fc = new FileChooser(Configuration.getLastDirectory(),
					"Select Jar Export",
					"Jar Archives",
					"jar");
			
			int returnVal = fc.showSaveDialog(BytecodeViewer.viewer);
			if (returnVal == JFileChooser.APPROVE_OPTION)
			{
				Configuration.lastDirectory = fc.getSelectedFile().getAbsolutePath();
				File file = fc.getSelectedFile();
				String path = file.getAbsolutePath();
				
				//auto append .jar
				if (!path.endsWith(".jar"))
					path = path + ".jar";
				
				if (!DialogueUtils.canOverwriteFile(path))
					return;
				
				new ExportJar(path).setVisible(true);
			}
		}, "Runnable Jar Export");
		exportThread.start();
	}
}
