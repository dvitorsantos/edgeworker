package lsdi.edgeworker.Services;

import com.espertech.esper.common.client.EPCompiled;
import com.espertech.esper.compiler.client.EPCompileException;
import com.espertech.esper.runtime.client.EPDeployException;
import com.espertech.esper.runtime.client.EPDeployment;
import com.espertech.esper.runtime.client.EPStatement;
import com.espertech.esper.runtime.client.EPUndeployException;
import lsdi.edgeworker.DataTransferObjects.DeployRequest;
import lsdi.edgeworker.DataTransferObjects.DeployResponse;
import lsdi.edgeworker.DataTransferObjects.RuleRequestResponse;
import lsdi.edgeworker.Listeners.EventListener;
import org.springframework.stereotype.Service;

@Service
public class DeployService {
    String hostUuid = System.getenv("EDGEWORKER_UUID");
    EsperService esperService = EsperService.getInstance();

    public DeployResponse deploy(RuleRequestResponse edgeRule) {
        try {
            EPCompiled epCompiled = esperService.compile(EsperService.buildEPL(edgeRule));
            EPDeployment epDeployment = esperService.deploy(epCompiled);
            EPStatement epStatement = esperService.getStatement(epDeployment.getDeploymentId(), edgeRule.getName());
            epStatement.addListener(new EventListener(edgeRule.getEventType(), edgeRule.getWebhookUrl()));

            return new DeployResponse(hostUuid, epDeployment.getDeploymentId(), edgeRule.getUuid(), "DONE");
        } catch (EPCompileException | EPDeployException exception) {
            exception.printStackTrace();
            return new DeployResponse(hostUuid, null, edgeRule.getUuid(), "ERROR");
        }
    }

    public DeployResponse undeploy(String deploymentId) {
        try {
            esperService.undeploy(deploymentId);
            return new DeployResponse(hostUuid, deploymentId, null, "UNDEPLOYED");
        } catch (EPUndeployException exception) {
            exception.printStackTrace();
            return new DeployResponse(hostUuid, deploymentId, null, "ERROR");
        }
    }
}
