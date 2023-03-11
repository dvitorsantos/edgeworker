package lsdi.edgeworker.Controllers;

import com.espertech.esper.common.client.EPCompiled;
import com.espertech.esper.compiler.client.EPCompileException;
import com.espertech.esper.runtime.client.*;
import lsdi.edgeworker.DataTransferObjects.DeployRequest;
import lsdi.edgeworker.DataTransferObjects.DeployResponse;
import lsdi.edgeworker.Listeners.EventListener;
import lsdi.edgeworker.Services.EsperService;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;


@RestController
public class DeployController {
    EsperService esperService = EsperService.getInstance();

    @PostMapping("/deploy")
    public Object deploy(@RequestBody DeployRequest deployRequest) {
        try {
            EPCompiled epCompiled = esperService.compile(EsperService.buildEPL(deployRequest));
            EPDeployment epDeployment = esperService.deploy(epCompiled);
            EPStatement epStatement = esperService.getStatement(epDeployment.getDeploymentId(), deployRequest.getName());
            epStatement.addListener(new EventListener());

            return new DeployResponse(
                    epDeployment.getDeploymentId(),
                    deployRequest.getName(),
                    deployRequest.getRule(),
                    "Deployed successfully.");
        } catch (EPCompileException | EPDeployException exception) {
            exception.printStackTrace();
            return "Something went wrong.";
        }
    }

    @PostMapping("/undeploy/{deploymentId}")
    public Object undeploy(@PathVariable String deploymentId) {
        try {
            esperService.undeploy(deploymentId);
            return "Undeploy successfully.";
        } catch (EPUndeployException exception) {
            exception.printStackTrace();
            return "Something went wrong.";
        }
    }
}
