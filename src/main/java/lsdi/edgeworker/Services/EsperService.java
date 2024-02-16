package lsdi.edgeworker.Services;

import com.espertech.esper.common.client.EPCompiled;
import com.espertech.esper.common.client.configuration.Configuration;
import com.espertech.esper.compiler.client.CompilerArguments;
import com.espertech.esper.compiler.client.EPCompileException;
import com.espertech.esper.compiler.client.EPCompiler;
import com.espertech.esper.compiler.client.EPCompilerProvider;
import com.espertech.esper.runtime.client.*;
import lombok.Data;
import lsdi.edgeworker.Models.Vehicle;
import lsdi.edgeworker.Requests.RuleRequest;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Service;

@Data
@Service
public final class EsperService {
    private Configuration configuration;
    private CompilerArguments arguments;
    private EPCompiler compiler;
    private EPRuntime runtime;
    private static EsperService instance;
    private Map<String, String> deployedRulesMap;

    public EsperService() {
        configuration = new Configuration();
        configuration.getCommon().addEventType("Vehicle", Vehicle.class);
        arguments = new CompilerArguments(configuration);
        compiler = EPCompilerProvider.getCompiler();
        runtime = EPRuntimeProvider.getDefaultRuntime(configuration);
        this.deployedRulesMap = new HashMap<>();
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

    public void undeploy(String ruleUuid) throws EPUndeployException {
        String deploymentUuid = deployedRulesMap.get(ruleUuid);
        runtime.getDeploymentService().undeploy(deploymentUuid);
    }
    
    public void sendEvent(Vehicle event, String type) {
        runtime.getEventService().sendEventBean(event, type);
    }

    public EPStatement getStatement(String deploymentId, String statementName) {
        return runtime.getDeploymentService().getStatement(deploymentId, statementName);
    }

    public void saveDeployedRule(String ruleUuid, String deploymentUuid) {
        this.deployedRulesMap.put(ruleUuid, deploymentUuid);
    }

    public static String buildEPL(RuleRequest edgeRule) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("@Name('");
        stringBuilder.append(edgeRule.getName());
        stringBuilder.append("')\n");
        stringBuilder.append(edgeRule.getDefinition());
        stringBuilder.append(";\n");
        return stringBuilder.toString();
    }
}
