package de.dbanalytics.spic.job;

import de.dbanalytics.spic.data.Person;
import junit.framework.Assert;
import junit.framework.TestCase;

import java.util.Collection;
import java.util.HashSet;


public class TestShellCommandJob extends TestCase {

    public void testCommand(){
        ShellCommandJob job = new ShellCommandJob();
        job.setCommand("ls -la");
        Collection<? extends Person> result = job.execute(new HashSet<>());
        Assert.assertNotNull(result);
    }
}
