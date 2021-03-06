package com.camunda.training;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.assertj.core.api.Assertions;
import org.camunda.bpm.dmn.engine.DmnDecisionRuleResult;
import org.camunda.bpm.dmn.engine.DmnDecisionTableResult;
import org.camunda.bpm.engine.externaltask.ExternalTask;
import org.camunda.bpm.engine.runtime.Job;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.mock.Mocks;
import org.camunda.bpm.extension.process_test_coverage.junit.rules.TestCoverageProcessEngineRuleBuilder;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import static org.camunda.bpm.engine.test.assertions.ProcessEngineTests.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.anyString;

public class ProcessJUnitTest {

  @Mock
  private TwitterService twitterService;

  @Rule
  public ProcessEngineRule rule = TestCoverageProcessEngineRuleBuilder
          .create()
          .build();

  @Before
  public void setup() {
    MockitoAnnotations.initMocks(this);
    init(rule.getProcessEngine());
    Mocks.register("tweetDelegate", new TweetDelegate(twitterService));
  }


  @Test
  @Deployment(resources = {"autoApproval.dmn", "training.bpmn"})
  public void testHappyPath(){
    //given
    Mockito.when(twitterService.sendTweet(anyString())).thenReturn(true);

    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("Email", "");
    variables.put("Tweet", "camunda rocks!"+ UUID.randomUUID());
    variables.put("approval", true);

    //When
    ProcessInstance processInstance = runtimeService()
            .startProcessInstanceByKey("Process_TwitterQA", variables);
    List<Job> jobList = jobQuery()
            .processInstanceId(processInstance.getId())
            .list();
    Job job = jobList.get(0);
    execute(job);

    // Then
    assertThat(processInstance).isEnded().hasPassed("End_Event_Published");
    Mockito.verify(twitterService).sendTweet(anyString());
  }

  @Test
  @Deployment(resources = {"autoApproval.dmn", "training.bpmn"})
  public void testSadPath() {
    // Given
    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("Tweet", "Make Camunda Great Again!");
    variables.put("approval", false);

    // When
    ProcessInstance processInstance = runtimeService()
            .createProcessInstanceByKey("Process_TwitterQA")
            .setVariables(variables)
            .startAfterActivity("Approve_Tweet")
            .execute();
    assertThat(processInstance).isWaitingAt("Feedback_Tweet");
    complete(externalTask());

    // Then
    assertThat(processInstance).isEnded();
    assertThat(processInstance).hasPassed("End_Event_Rejected");
  }

  @Test
  @Deployment(resources = {"autoApproval.dmn", "training.bpmn"})
  public void testSuperUserMessageEvents(){
    // Given
    Mockito.when(twitterService.sendTweet(anyString())).thenReturn(true);
    ProcessInstance processInstance = runtimeService()
            .createMessageCorrelation("superuserTweet")
            .setVariable("Tweet", "I'm a superuser and can do whatever I want!")
            .correlateWithResult()
            .getProcessInstance();

    // when
    assertThat(processInstance).isStarted();
    List<Job> jobList = jobQuery()
            .processInstanceId(processInstance.getId())
            .list();
    assertEquals(1, jobList.size());
    Job job = jobList.get(0);
    execute(job);

    // Then
    assertThat(processInstance).isEnded();
    assertThat(processInstance).hasPassed("End_Event_Published");
    Mockito.verify(twitterService).sendTweet(anyString());
  }


  @Test
  @Deployment(resources = {"autoApproval.dmn", "training.bpmn"})
  public void testTweetFromJakob() {
    // Given
    Mockito.when(twitterService.sendTweet(anyString())).thenReturn(true);

    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("Email", "jakob.freund@camunda.com");
    variables.put("Tweet", "this needs to be published "+ UUID.randomUUID());
    variables.put("approval", true);

    //When
    ProcessInstance processInstance = runtimeService()
            .startProcessInstanceByKey("Process_TwitterQA", variables);
    List<Job> jobList = jobQuery()
            .processInstanceId(processInstance.getId())
            .list();
    Job job = jobList.get(0);
    execute(job);

    // Then
    assertThat(processInstance).isEnded().hasPassed("End_Event_Published");
    Mockito.verify(twitterService).sendTweet(anyString());
  }
}