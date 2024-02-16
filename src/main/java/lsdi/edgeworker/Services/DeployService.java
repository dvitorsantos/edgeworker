package lsdi.edgeworker.Services;

import com.espertech.esper.common.client.EPCompiled;
import com.espertech.esper.compiler.client.EPCompileException;
import com.espertech.esper.runtime.client.EPDeployException;
import com.espertech.esper.runtime.client.EPDeployment;
import com.espertech.esper.runtime.client.EPStatement;
import com.espertech.esper.runtime.client.EPUndeployException;

import lsdi.edgeworker.Listeners.EventListener;
import lsdi.edgeworker.Requests.DeployResponse;
import lsdi.edgeworker.Requests.RuleRequest;

import org.springframework.stereotype.Service;
@Service
public class DeployService {
    String hostUuid = System.getenv("EDGEWORKER_UUID");
    EsperService esperService = EsperService.getInstance();

    public DeployResponse deploy(RuleRequest rule) {
        try {
            EPCompiled epCompiled = esperService.compile(EsperService.buildEPL(rule));
            EPDeployment epDeployment = esperService.deploy(epCompiled);
            EPStatement epStatement = esperService.getStatement(epDeployment.getDeploymentId(), rule.getName());
            epStatement.addListener(new EventListener(rule, null));

            esperService.saveDeployedRule(rule.getUuid(), epDeployment.getDeploymentId());
            return new DeployResponse(hostUuid, epDeployment.getDeploymentId(), rule.getUuid(), "DEPLOYED");
        } catch (EPCompileException | EPDeployException exception) {
            exception.printStackTrace();
            return new DeployResponse(hostUuid, null, rule.getUuid(), "ERROR");
        }
    }

    public DeployResponse undeploy(String ruleUuid) {
        try {
            esperService.undeploy(ruleUuid);
            System.out.println("(INFO): rule " + ruleUuid + " undeployed.");
            return new DeployResponse(hostUuid, null, ruleUuid, "UNDEPLOYED");
        } catch (EPUndeployException exception) {
            System.out.println("(ERROR): " + exception.getMessage());
            return new DeployResponse(hostUuid, null, ruleUuid, "ERROR");
        }
    }
}
