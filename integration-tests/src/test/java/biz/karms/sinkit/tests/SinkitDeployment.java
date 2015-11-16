package biz.karms.sinkit.tests;

import biz.karms.sinkit.tests.api.ApiIntegrationTest;
import biz.karms.sinkit.tests.core.CoreTest;
import biz.karms.sinkit.tests.util.IoCFactory;
import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import org.eu.ingwar.tools.arquillian.extension.suite.annotations.ArquillianSuiteDeployment;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.importer.ZipImporter;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
//import org.jboss.weld.exceptions.DeploymentException;
//import org.jboss.weld.exceptions.IllegalArgumentException;

import java.io.File;

/**
 * @author Michal Karm Babacek
 */
@ArquillianSuiteDeployment
public class SinkitDeployment {
    @Deployment(name = "ear", testable = true, managed = true)
    public static Archive<?> createTestArchive() {
        EnterpriseArchive ear = ShrinkWrap.create(ZipImporter.class, "sinkit-ear.ear").importFrom(new File("../ear/target/sinkit-ear.ear")).as(EnterpriseArchive.class);
        ear.getAsType(JavaArchive.class, "sinkit-ejb.jar")
                .addClass(ApiIntegrationTest.class)
                .addClass(CoreTest.class)
                .addClass(IoCFactory.class)
                .addClass(FailingHttpStatusCodeException.class);
                //.addClass(DeploymentException.class);
        return ear;
    }
}
