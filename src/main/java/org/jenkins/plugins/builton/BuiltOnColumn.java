package org.jenkins.plugins.builton;

import hudson.Extension;
import hudson.model.Descriptor;
import hudson.views.ListViewColumn;

import org.kohsuke.stapler.DataBoundConstructor;

public class BuiltOnColumn extends ListViewColumn{
	@DataBoundConstructor
	public BuiltOnColumn() {}
	
    @Extension
    public static class DescriptorImpl extends Descriptor<ListViewColumn> {
        @Override
        public String getDisplayName() {
            return Messages.BuiltOnColumn_DisplayName();
        }
    }
}
