package org.jenkins.plugins.builton;

import edu.umd.cs.findbugs.annotations.CheckForNull;
import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Extension;
import hudson.model.AbstractBuild;
import hudson.model.Descriptor;
import hudson.model.Job;
import hudson.model.Node;
import hudson.model.Run;
import hudson.views.ListViewColumn;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import jenkins.model.Jenkins;
import org.jenkinsci.plugins.workflow.actions.WorkspaceAction;
import org.jenkinsci.plugins.workflow.flow.FlowExecution;
import org.jenkinsci.plugins.workflow.graph.FlowGraphWalker;
import org.jenkinsci.plugins.workflow.graph.FlowNode;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.kohsuke.stapler.DataBoundConstructor;

public class BuiltOnColumn extends ListViewColumn {

    @DataBoundConstructor
    public BuiltOnColumn() {}

    /**
     * Resolves the node(s) on which the last build of the given job ran.
     * Supports both Freestyle/AbstractBuild jobs and Pipeline (WorkflowJob) runs
     * when the optional Pipeline plugins are installed.
     */
    @NonNull
    public List<BuiltOnNode> getBuiltOnNodes(@CheckForNull Job<?, ?> job) {
        if (job == null) {
            return Collections.emptyList();
        }
        Run<?, ?> lastBuild = job.getLastBuild();
        if (lastBuild == null) {
            return Collections.emptyList();
        }
        if (lastBuild instanceof AbstractBuild<?, ?> abstractBuild) {
            Node builtOn = abstractBuild.getBuiltOn();
            if (builtOn != null) {
                return List.of(new BuiltOnNode(builtOn.getDisplayName(), builtOn.getSearchUrl()));
            }
            String builtOnStr = abstractBuild.getBuiltOnStr();
            if (builtOnStr != null && !builtOnStr.isBlank()) {
                return List.of(new BuiltOnNode(builtOnStr, null));
            }
            return Collections.emptyList();
        }
        if (isPipelinePluginInstalled()) {
            return PipelineNodeResolver.resolve(lastBuild);
        }
        return Collections.emptyList();
    }

    private static boolean isPipelinePluginInstalled() {
        Jenkins jenkins = Jenkins.getInstanceOrNull();
        return jenkins != null
                && jenkins.getPlugin("workflow-job") != null
                && jenkins.getPlugin("workflow-api") != null;
    }

    private static BuiltOnNode toBuiltOnNode(String nodeName) {
        Jenkins jenkins = Jenkins.get();
        if (nodeName.isEmpty()) {
            return new BuiltOnNode(jenkins.getDisplayName(), jenkins.getSearchUrl());
        }
        Node node = jenkins.getNode(nodeName);
        if (node != null) {
            return new BuiltOnNode(node.getDisplayName(), node.getSearchUrl());
        }
        return new BuiltOnNode(nodeName, null);
    }

    /**
     * Isolated helper so Pipeline classes are only loaded when optional workflow plugins are present.
     */
    private static final class PipelineNodeResolver {
        private static List<BuiltOnNode> resolve(Run<?, ?> run) {
            if (!(run instanceof WorkflowRun workflowRun)) {
                return Collections.emptyList();
            }
            FlowExecution execution = workflowRun.getExecution();
            if (execution == null) {
                return Collections.emptyList();
            }
            List<String> discovered = new ArrayList<>();
            FlowGraphWalker walker = new FlowGraphWalker(execution);
            for (FlowNode flowNode : walker) {
                WorkspaceAction ws = flowNode.getAction(WorkspaceAction.class);
                if (ws != null) {
                    discovered.add(ws.getNode());
                }
            }
            if (discovered.isEmpty()) {
                return Collections.emptyList();
            }
            Collections.reverse(discovered);
            Set<String> uniqueNames = new LinkedHashSet<>(discovered);
            List<BuiltOnNode> result = new ArrayList<>(uniqueNames.size());
            for (String nodeName : uniqueNames) {
                result.add(toBuiltOnNode(nodeName));
            }
            return result;
        }
    }

    public static final class BuiltOnNode {
        private final String displayName;
        private final String searchUrl;

        public BuiltOnNode(@NonNull String displayName, @CheckForNull String searchUrl) {
            this.displayName = displayName;
            this.searchUrl = searchUrl;
        }

        @NonNull
        public String getDisplayName() {
            return displayName;
        }

        @CheckForNull
        public String getSearchUrl() {
            return searchUrl;
        }
    }

    @Extension
    public static class DescriptorImpl extends Descriptor<ListViewColumn> {
        @Override
        public String getDisplayName() {
            return Messages.BuiltOnColumn_DisplayName();
        }
    }
}
