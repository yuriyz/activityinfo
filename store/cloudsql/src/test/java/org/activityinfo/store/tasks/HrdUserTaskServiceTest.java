package org.activityinfo.store.tasks;

import org.activityinfo.model.auth.AuthenticatedUser;
import org.activityinfo.service.tasks.UserTask;
import org.activityinfo.service.tasks.UserTaskStatus;
import org.activityinfo.store.hrd.TestingEnvironment;
import org.junit.Rule;
import org.junit.Test;

import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.lessThan;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.junit.Assert.assertThat;

public class HrdUserTaskServiceTest {

    @Rule
    public TestingEnvironment environment = new TestingEnvironment();


    @Test
    public void test() {
        UserTask userTask = environment.getTaskService().startTask(me(), "Import Job");
        assertThat(userTask.getStatus(), equalTo(UserTaskStatus.RUNNING));
        assertThat(userTask.getDescription(), equalTo("Import Job"));
        assertThat(userTask.getTimeStarted(), lessThan((double)System.currentTimeMillis()));

        // ensure that the task is listed
        List<UserTask> tasks = environment.getTaskService().getUserTasks(me());
        assertThat(tasks, hasSize(1));
        assertThat(tasks.get(0).getStatus(), equalTo(UserTaskStatus.RUNNING));
        assertThat(tasks.get(0).getDescription(), equalTo("Import Job"));
        assertThat(tasks.get(0).getId(), equalTo(userTask.getId()));

        // update the task
        environment.getTaskService().updateTask(me(), userTask.getId(), UserTaskStatus.COMPLETE);

        // ensure that the task is listed
        List<UserTask> updatedTasks = environment.getTaskService().getUserTasks(me());
        assertThat(updatedTasks, hasSize(1));
        assertThat(updatedTasks.get(0).getStatus(), equalTo(UserTaskStatus.COMPLETE));

        // and that we can fetch by id
        UserTask updated = environment.getTaskService().getUserTask(environment.getUser(), userTask.getId());
        assertThat(updated.getId(), equalTo(updated.getId()));
        assertThat(updated.getStatus(), equalTo(UserTaskStatus.COMPLETE));
        assertThat(updated.getDescription(), equalTo("Import Job"));
    }

    private AuthenticatedUser me() {
        return environment.getUser();
    }
}