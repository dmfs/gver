package org.dmfs.gradle.gitversion.tasks;

import org.gradle.api.DefaultTask;
import org.gradle.api.Task;
import org.gradle.api.tasks.TaskAction;


/**
 * A {@link Task} that logs the current version.
 */
public class VersionTask extends DefaultTask
{

    public VersionTask()
    {
        // TODO: is there a better place/way to set these?
        setGroup("gitVersion");
        setDescription("show the current project version");
    }


    @TaskAction
    public void perform()
    {
        System.out.println(getProject().getVersion());
    }
}
