package org.jenkins.plugins.builton;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import hudson.model.FreeStyleProject;
import hudson.model.ListView;
import hudson.slaves.DumbSlave;
import java.util.List;
import org.htmlunit.html.HtmlPage;
import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.junit.jupiter.api.Test;
import org.jvnet.hudson.test.Issue;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.junit.jupiter.WithJenkins;

@WithJenkins
class BuiltOnColumnPipelineTest {

    @Test
    @Issue("JENKINS-38486")
    void showsAgentForPipelineJobInListView(JenkinsRule j) throws Exception {
        DumbSlave agent = j.createOnlineSlave();
        WorkflowJob pipeline = j.createProject(WorkflowJob.class, "pipeline-agent-job");
        pipeline.setDefinition(new CpsFlowDefinition("node('" + agent.getNodeName() + "') { echo 'hello' }", true));
        j.buildAndAssertSuccess(pipeline);

        BuiltOnColumn column = new BuiltOnColumn();
        List<BuiltOnColumn.BuiltOnNode> nodes = column.getBuiltOnNodes(pipeline);
        assertEquals(1, nodes.size());
        assertEquals(agent.getDisplayName(), nodes.get(0).getDisplayName());
        assertEquals(agent.getSearchUrl(), nodes.get(0).getSearchUrl());

        ListView view = new ListView("pipeline-view", j.jenkins);
        j.jenkins.addView(view);
        view.add(pipeline);
        view.setColumns(List.of(column));

        try (JenkinsRule.WebClient wc = j.createWebClient()) {
            HtmlPage page = wc.goTo("view/pipeline-view/");
            String html = page.getWebResponse().getContentAsString();
            assertTrue(html.contains(agent.getDisplayName()), "ListView should show Pipeline agent display name");
            assertTrue(html.contains(agent.getSearchUrl()), "ListView should link to Pipeline agent searchUrl");
        }
    }

    @Test
    @Issue("JENKINS-38486")
    void showsBuiltInNodeForPipelineJobRunningOnController(JenkinsRule j) throws Exception {
        WorkflowJob pipeline = j.createProject(WorkflowJob.class, "pipeline-controller-job");
        pipeline.setDefinition(new CpsFlowDefinition("node('') { echo 'controller' }", true));
        j.buildAndAssertSuccess(pipeline);

        BuiltOnColumn column = new BuiltOnColumn();
        List<BuiltOnColumn.BuiltOnNode> nodes = column.getBuiltOnNodes(pipeline);
        assertEquals(1, nodes.size());
        assertEquals(j.jenkins.getDisplayName(), nodes.get(0).getDisplayName());
        assertEquals(j.jenkins.getSearchUrl(), nodes.get(0).getSearchUrl());
    }

    @Test
    void rendersEmptyCellWithoutBrokenLinkForUnbuiltJob(JenkinsRule j) throws Exception {
        FreeStyleProject unbuilt = j.createFreeStyleProject("never-built-job");
        BuiltOnColumn column = new BuiltOnColumn();
        assertTrue(column.getBuiltOnNodes(unbuilt).isEmpty());

        ListView view = new ListView("unbuilt-view", j.jenkins);
        j.jenkins.addView(view);
        view.add(unbuilt);
        view.setColumns(List.of(column));

        try (JenkinsRule.WebClient wc = j.createWebClient()) {
            HtmlPage page = wc.goTo("view/unbuilt-view/");
            String html = page.getWebResponse().getContentAsString();
            assertFalse(html.contains("<a href=\"/jenkins/\" class=\"model-link\"></a>"));
        }
    }
}
