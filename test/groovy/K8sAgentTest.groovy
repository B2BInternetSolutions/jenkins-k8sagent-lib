package groovy

import com.lesfurets.jenkins.unit.BasePipelineTest
import org.junit.Before
import org.junit.Test
import com.github.liejuntao001.jenkins.MyYaml

import static junit.framework.Assert.assertEquals

class K8sAgentTest extends BasePipelineTest {
  def agent
  def parser

  def base_yaml = """spec:
  hostAliases:
  - ip: "192.168.1.15"
    hostnames:
    - "jenkins.example.com"
  volumes:
  - hostPath:
      path: /data/jenkins/repo_mirror
      type: ""
    name: volume-0
  containers:
  - name: jnlp
    image: jenkinsci/jnlp-slave:3.29-1
    imagePullPolicy: Always
    command:
    - /usr/local/bin/jenkins-slave
    volumeMounts:
    - mountPath: /home/jenkins/repo_cache
      name: volume-0"""

  def small_yaml = """spec:
  hostAliases:
  - ip: "192.168.1.15"
    hostnames:
    - "jenkins.example.com"
  volumes:
  - hostPath:
      path: /data/jenkins/repo_mirror
      type: ""
    name: volume-0
  containers:
  - name: jnlp
    image: jenkinsci/jnlp-slave:3.29-1
    imagePullPolicy: Always
    command:
    - /usr/local/bin/jenkins-slave
    volumeMounts:
    - mountPath: /home/jenkins/repo_cache
      name: volume-0
    resources:
      limits:
        memory: 8Gi
      requests:
        memory: 4Gi
        cpu: 2"""

  def fast_yaml = """spec:
  hostAliases:
  - ip: "192.168.1.15"
    hostnames:
    - "jenkins.example.com"
  volumes:
  - hostPath:
      path: /data/jenkins/repo_mirror
      type: ""
    name: volume-0
  containers:
  - name: jnlp
    image: jenkinsci/jnlp-slave:3.29-1
    imagePullPolicy: Always
    command:
    - /usr/local/bin/jenkins-slave
    volumeMounts:
    - mountPath: /home/jenkins/repo_cache
      name: volume-0
    resources:
      limits:
        memory: 31Gi
        cpu: 14
      requests:
        memory: 16Gi
        cpu: 8
  nodeSelector:
    builder: 'fast'"""

  def small_pg_yaml = """spec:
  hostAliases:
  - ip: "192.168.1.15"
    hostnames:
    - "jenkins.example.com"
  volumes:
  - hostPath:
      path: /data/jenkins/repo_mirror
      type: ""
    name: volume-0
  containers:
  - name: jnlp
    image: jenkinsci/jnlp-slave:3.29-1
    imagePullPolicy: Always
    command:
    - /usr/local/bin/jenkins-slave
    volumeMounts:
    - mountPath: /home/jenkins/repo_cache
      name: volume-0
    resources:
      limits:
        memory: 8Gi
      requests:
        memory: 4Gi
        cpu: 2
  - name: pg
    image: postgres:9.5.19
    tty: true"""

  def small_pg_sdk25_yaml = """spec:
  hostAliases:
  - ip: "192.168.1.15"
    hostnames:
    - "jenkins.example.com"
  volumes:
  - hostPath:
      path: /data/jenkins/repo_mirror
      type: ""
    name: volume-0
  containers:
  - name: jnlp
    image: jenkinsci/jnlp-slave:3.29-1
    imagePullPolicy: Always
    command:
    - /usr/local/bin/jenkins-slave
    volumeMounts:
    - mountPath: /home/jenkins/repo_cache
      name: volume-0
    resources:
      limits:
        memory: 8Gi
      requests:
        memory: 4Gi
        cpu: 2
    env:
    - name: USE_SDK25
      value: true
  - name: pg
    image: postgres:9.5.19
    tty: true"""

  def small_privileged_yaml = """spec:
  hostAliases:
  - ip: "192.168.1.15"
    hostnames:
    - "jenkins.example.com"
  volumes:
  - hostPath:
      path: /data/jenkins/repo_mirror
      type: ""
    name: volume-0
  containers:
  - name: jnlp
    image: jenkinsci/jnlp-slave:3.29-1
    imagePullPolicy: Always
    command:
    - /usr/local/bin/jenkins-slave
    volumeMounts:
    - mountPath: /home/jenkins/repo_cache
      name: volume-0
    resources:
      limits:
        memory: 8Gi
      requests:
        memory: 4Gi
        cpu: 2
    securityContext:
      privileged: true
  affinity:
    podAntiAffinity:
      requiredDuringSchedulingIgnoredDuringExecution:
      - labelSelector:
          matchExpressions:
          - key: privileged
            operator: In
            values:
            - true
        topologyKey: kubernetes.io/hostname
metadata:
  labels:
    privileged: true"""

  @Before
  void setUp() {
    super.setUp()
    // load
    agent = loadScript("vars/k8sagent.groovy")
    parser = new MyYaml()

  }

  @Test
  void testBase() {
    def expected_yaml = base_yaml
    def processed_yaml = parser.merge([expected_yaml.toString()])

    def expected = [
        cloud: 'kubernetes',
        yaml : processed_yaml
    ]

    def ret = agent(name: 'base')

    assertEquals "results", ret.entrySet().containsAll(expected.entrySet()), true
    //assertEquals "results", expected.entrySet(), ret.entrySet()
  }

  @Test
  void testSmall() {
    def expected_yaml = small_yaml
    def processed_yaml = parser.merge([expected_yaml.toString()])

    def expected = [
        cloud: 'kubernetes',
        yaml : processed_yaml
    ]

    def ret = agent(name: 'small')

    assertEquals "results", ret.entrySet().containsAll(expected.entrySet()), true
    //assertEquals "results", expected.entrySet(), ret.entrySet()

  }

  @Test
  void testFast() {
    def expected_yaml = fast_yaml
    def processed_yaml = parser.merge([expected_yaml.toString()])

    def expected = [
        cloud: 'kubernetes',
        yaml : processed_yaml
    ]

    def ret = agent(name: 'fast')

    assertEquals "results", ret.entrySet().containsAll(expected.entrySet()), true
    //assertEquals "results", expected.entrySet(), ret.entrySet()
  }

  @Test
  void testPg() {
    def expected_yaml = small_pg_yaml
    def processed_yaml = parser.merge([expected_yaml.toString()])

    def expected = [
        cloud: 'kubernetes',
        yaml : processed_yaml
    ]

    def ret = agent(name: 'small+pg')

    assertEquals "results", ret.entrySet().containsAll(expected.entrySet()), true
    //assertEquals "results", expected.entrySet(), ret.entrySet()
  }

  @Test
  void testSdk25() {
    def expected_yaml = small_pg_sdk25_yaml
    def processed_yaml = parser.merge([expected_yaml.toString()])

    def expected = [
        cloud: 'kubernetes',
        yaml : processed_yaml
    ]

    def ret = agent(name: 'small+pg+sdk25')

    assertEquals "results", ret.entrySet().containsAll(expected.entrySet()), true
    //assertEquals "results", expected.entrySet(), ret.entrySet()
  }

  @Test
  void testPrivileged() {
    def expected_yaml = small_privileged_yaml
    def processed_yaml = parser.merge([expected_yaml.toString()])

    def expected = [
        cloud: 'kubernetes',
        yaml : processed_yaml
    ]

    def ret = agent(name: 'small+privileged')

    assertEquals "results", ret.entrySet().containsAll(expected.entrySet()), true
    //assertEquals "results", expected.entrySet(), ret.entrySet()
  }
}
