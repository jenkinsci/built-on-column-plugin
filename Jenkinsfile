library identifier: 'mwaite-test@use-windows-vm',
        retriever: legacySCM(clone: true,
                             scm: scmGit(branches: [[name: 'use-windows-vm']],
                                         extensions: [cloneOption(depth: 1, honorRefspec: true, noTags: true, shallow: true)],
                                         gitTool: 'git-native',
                                         userRemoteConfigs: [[refspec: '+refs/heads/use-windows-vm:refs/remotes/origin/use-windows-vm',
                                                              url: 'https://github.com/MarkEWaite/pipeline-library.git']]))

/*
 See the documentation for more options:
 https://github.com/jenkins-infra/pipeline-library/
*/
buildPlugin(
  useContainerAgent: false, // Set to `false` if you need to use Docker for containerized tests
  configurations: [
    [platform: 'linux', jdk: 21],
    [platform: 'windows', jdk: 17],
])
