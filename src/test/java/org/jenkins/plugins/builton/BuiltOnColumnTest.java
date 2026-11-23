package org.jenkins.plugins.builton;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import hudson.model.FreeStyleProject;
import hudson.model.ListView;
import hudson.slaves.DumbSlave;
import java.util.List;
import org.htmlunit.html.HtmlPage;
import org.junit.jupiter.api.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.junit.jupiter.WithJenkins;

@WithJenkins
class BuiltOnColumnTest {

    @Test
    void descriptorHasExpectedDisplayName(JenkinsRule j) {
        BuiltOnColumn column = new BuiltOnColumn();
        assertEquals(
                Messages.BuiltOnColumn_DisplayName(), column.getDescriptor().getDisplayName());
    }

    @Test
    void rendersBuiltOnAgentInListView(JenkinsRule j) throws Exception {
        DumbSlave agent = j.createOnlineSlave();
        FreeStyleProject project = j.createFreeStyleProject("freestyle-agent-job");
        project.setAssignedNode(agent);
        j.buildAndAssertSuccess(project);

        ListView view = new ListView("built-on-view", j.jenkins);
        j.jenkins.addView(view);
        view.add(project);
        view.setColumns(List.of(new BuiltOnColumn()));

        try (JenkinsRule.WebClient wc = j.createWebClient()) {
            HtmlPage page = wc.goTo("view/built-on-view/");
            String html = page.getWebResponse().getContentAsString();
            assertTrue(
                    html.contains(agent.getDisplayName()),
                    "ListView should display the agent display name in the Built On column");
            assertTrue(
                    html.contains(agent.getSearchUrl()),
                    "ListView should link to the agent search URL in the Built On column");
        }
    }
}
