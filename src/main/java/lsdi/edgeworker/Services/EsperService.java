package lsdi.edgeworker.Services;

import com.espertech.esper.common.client.EPCompiled;
import com.espertech.esper.common.client.configuration.Configuration;
import com.espertech.esper.compiler.client.CompilerArguments;
import com.espertech.esper.compiler.client.EPCompileException;
import com.espertech.esper.compiler.client.EPCompiler;
import com.espertech.esper.compiler.client.EPCompilerProvider;
import com.espertech.esper.runtime.client.*;
import lombok.Data;
import lsdi.edgeworker.DataTransferObjects.DeployRequest;
import lsdi.edgeworker.Models.SmartMeterMeasurement;
import org.springframework.stereotype.Service;

@Data
@Service
public final class EsperService {
    private Configuration configuration;
    private CompilerArguments arguments;
    private EPCompiler compiler;
    private EPRuntime runtime;
    private static EsperService instance;

    public EsperService() {
        configuration = new Configuration();
        configuration.getCommon().addEventType("SmartMeterMeasurement", SmartMeterMeasurement.class);
        arguments = new CompilerArguments(configuration);
        compiler = EPCompilerProvider.getCompiler();
        runtime = EPRuntimeProvider.getDefaultRuntime(configuration);
    }

    public static EsperService getInstance() {
        if (instance == null) instance = new EsperService();
        return instance;
    }

    public EPCompiled compile(String epl) throws EPCompileException {
        return compiler.compile(epl, arguments);
    }

    public EPDeployment deploy(EPCompiled compiled) throws EPDeployException {
        return runtime.getDeploymentService().deploy(compiled);
    }

    public void undeploy(String deploymentId) throws EPUndeployException {
        runtime.getDeploymentService().undeploy(deploymentId);
    }

    public void sendEvent(SmartMeterMeasurement smartMeterMeasurement) {
        runtime.getEventService().sendEventBean(smartMeterMeasurement, "SmartMeterMeasurement");
    }

    public EPStatement getStatement(String deploymentId, String statementName) {
        return runtime.getDeploymentService().getStatement(deploymentId, statementName);
    }

    public static String buildEPL(DeployRequest deployRequest) {
        return "@Name('" + deployRequest.getName() + "')\n" + deployRequest.getRule() + ";\n";
    }
}
