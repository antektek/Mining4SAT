package execution;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

abstract class ExecuteCmd {

	protected final Process process;
	protected String[] cmd;

	public ExecuteCmd(String[] cmd) throws IOException, InterruptedException {
		this.cmd = cmd;
		Runtime runtime = Runtime.getRuntime();
		process = runtime.exec(cmd);
		process.waitFor();
	}

	protected Process getProcess() {
		return process;
	}

	void getStandardOutputStream(final Process process) {
		new Thread() {
			public void run() {
				try {
					BufferedReader reader = new BufferedReader(
							new InputStreamReader(process.getInputStream()));
					String line = "";
					try {
						while ((line = reader.readLine()) != null) {
							System.out.println(line);
						}
					} finally {
						reader.close();
					}
				} catch (IOException ioe) {
					ioe.printStackTrace();
				}
			}
		}.start();
	}

	void getErrorOutputStream(final Process process) {
		new Thread() {
			public void run() {
				try {
					BufferedReader reader = new BufferedReader(
							new InputStreamReader(process.getErrorStream()));
					String line = "";
					try {
						while ((line = reader.readLine()) != null) {
							System.err.println(line);
						}
					} finally {
						reader.close();
					}
				} catch (IOException ioe) {
					ioe.printStackTrace();
				}
			}
		}.start();
	}

	/**
	 * 
	 * @param cmd
	 * @return
	 * @throws IOException
	 * @throws InterruptedException
	 */
	abstract String launchCMD(String tdbFileName) throws IOException, InterruptedException;



}
