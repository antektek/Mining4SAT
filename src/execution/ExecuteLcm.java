package execution;

import java.io.File;
import java.io.IOException;

public class ExecuteLcm extends ExecuteCmd {

	public ExecuteLcm(String[] cmd) throws IOException, InterruptedException {
		super(cmd);
	}

	@Override
	String launchCMD(String tdbFileName) throws IOException, InterruptedException {
		
		getStandardOutputStream(getProcess());
		getErrorOutputStream(getProcess());
		if(getProcess().exitValue() !=0){
			new File(tdbFileName).delete();
			throw new RuntimeException("anomalies dans l'exection du lcm");
		}
		return cmd[cmd.length-1];

	}

	/**
	 * testes unitaires
	 * 
	 * @param args
	 */
	public static void main(String[] args) {

	}

}
