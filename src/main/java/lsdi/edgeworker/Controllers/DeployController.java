package lsdi.edgeworker.Controllers;

import com.espertech.esper.compiler.client.EPCompileException;
import com.espertech.esper.runtime.client.EPDeployException;
import lsdi.edgeworker.DataTransferObjects.DeployRequest;
import lsdi.edgeworker.Services.DeployService;
import org.springframework.web.bind.annotation.*;


@RestController
public class DeployController {
    DeployService deployService = new DeployService();

//    @PostMapping("/deploy")
//    public Object deploy(@RequestBody DeployRequest deployRequest) throws EPDeployException, EPCompileException {
//        return deployService.deploy(deployRequest);
//    }
//
//    @DeleteMapping("/undeploy/{deploymentId}")
//    public Object undeploy(@PathVariable String deploymentId) {
//        return deployService.undeploy(deploymentId);
//    }
}
