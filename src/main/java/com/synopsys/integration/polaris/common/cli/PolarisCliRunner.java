package com.synopsys.integration.polaris.common.cli;

import com.synopsys.integration.executable.ExecutableOutput;
import com.synopsys.integration.executable.ExecutableRunnerException;
import com.synopsys.integration.executable.ProcessBuilderRunner;

public class PolarisCliRunner {
    private ProcessBuilderRunner processBuilderRunner;

    public PolarisCliRunner(ProcessBuilderRunner processBuilderRunner) {
        this.processBuilderRunner = processBuilderRunner;
    }

    public ExecutableOutput execute(PolarisCliExecutable polarisCliExecutable) throws ExecutableRunnerException {
        return processBuilderRunner.execute(polarisCliExecutable);
    }

}
