# akka-sample-cluster-kubernetes-dns-java


Running the Kubernetes demos
The following steps work for the integration-test/kubernetes-api or the integration-test/kubernetes-dns sub-project:

To run the demo in a real Kubernetes or OpenShift cluster the images must be pushed to a registry that cluster has access to and then kubernetes/akka-cluster.yml (in either sub-project) modified with the full image path.

The following shows how the sample can be run in a local cluster using either minishift or minikube. Unless explicitly stated minikube can be replaced with minishift and kubectl with oc in any of the commands below.

Start minikube make sure you have installed and is running:

```bash
$ minikube start
```

Make sure your shell is configured to target the docker daemon running inside the VM:

```bash
$ eval $(minikube docker-env)
```
 
Publish the application docker image locally. If running this project in a real cluster you’ll need to publish the image to a repository that is accessible from your Kubernetes cluster and update the kubernetes/akka-cluster.yml with the new image name.

```bash
$ sbt shell
> project integration-test-kubernetes-api (or integration-test-kubernetes-dns)
> docker:publishLocal 
```
You can run multiple different Akka Bootstrap-based applications in the same namespace, alongside any other containers that belong to the same logical application. The resources in kubernetes/akka-cluster.yml are configured to run in the akka-bootstrap-demo-ns namespace. Change that to the namespace you want to deploy to. If you do not have a namespace to run your application in yet, create it:

```bash
kubectl create namespace <insert-namespace-name-here>

// and set it as the default for subsequent commands
kubectl config set-context $(kubectl config current-context) --namespace=<insert-namespace-name-here>
```

Or if running with minishift:

```bash
oc new-project <insert-namespace-name-here>

// and switch to that project to make it the default for subsequent comments
oc project <insert-namespace-name-here>
```

Next deploy the application:

```bash
// minikube using Kubernetes API
kubectl apply -f integration-test/kubernetes-api/kubernetes/akka-cluster.yml
```

or

```bash
// minikube using DNS
kubectl apply -f integration-test/kubernetes-dns/kubernetes/akka-cluster.yml
```

or

```bash
// minishift using Kubernetes API
oc apply -f integration-test/kubernetes-api/kubernetes/akka-cluster.yaml
```

or

```bash
// minishift using DNS
oc apply -f integration-test/kubernetes-dns/kubernetes/akka-cluster.yaml
```

This will create and start running a number of Pods hosting the application. The application nodes will form a cluster.

In order to observe the logs during the cluster formation you can pick one of the pods and issue the kubectl logs command on it:

```bash
$ POD=$(kubectl get pods | grep akka-bootstrap | grep Running | head -n1 | awk '{ print $1 }'); echo $POD
akka-integration-test-bcc456d8c-6qx87

$ kubectl logs $POD --follow | less
[INFO] [12/13/2018 07:13:42.867] [main] [ClusterBootstrap(akka://default)] Initiating bootstrap procedure using akka.discovery.akka-dns method...
[DEBUG] [12/13/2018 07:13:42.906] [default-akka.actor.default-dispatcher-2] [TimerScheduler(akka://default)] Start timer [resolve-key] with generation [1]
[DEBUG] [12/13/2018 07:13:42.919] [default-akka.actor.default-dispatcher-2] [TimerScheduler(akka://default)] Start timer [decide-key] with generation [2]
[INFO] [12/13/2018 07:13:42.924] [default-akka.actor.default-dispatcher-2] [akka.tcp://default@172.17.0.7:2552/system/bootstrapCoordinator] Locating service members. Using discovery [akka.discovery.dns.DnsSimpleServiceDiscovery], join decider [akka.management.cluster.bootstrap.LowestAddressJoinDecider]
[INFO] [12/13/2018 07:13:42.933] [default-akka.actor.default-dispatcher-2] [akka.tcp://default@172.17.0.7:2552/system/bootstrapCoordinator] Looking up [Lookup(integration-test-kubernetes-dns-internal.akka-bootstrap.svc.cluster.local,Some(management),Some(tcp))]
[DEBUG] [12/13/2018 07:13:42.936] [default-akka.actor.default-dispatcher-2] [DnsSimpleServiceDiscovery(akka://default)] Lookup [Lookup(integration-test-kubernetes-dns-internal.akka-bootstrap-demo-ns.svc.cluster.local,Some(management),Some(tcp))] translated to SRV query [_management._tcp.integration-test-kubernetes-dns-internal.akka-bootstrap-demo-ns.svc.cluster.local] as contains portName and protocol
[DEBUG] [12/13/2018 07:13:42.995] [default-akka.actor.default-dispatcher-18] [akka.tcp://default@172.17.0.7:2552/system/IO-DNS] Resolution request for _management._tcp.integration-test-kubernetes-dns-internal.akka-bootstrap-demo-ns.svc.cluster.local Srv from Actor[akka://default/temp/$a]
[DEBUG] [12/13/2018 07:13:43.011] [default-akka.actor.default-dispatcher-6] [akka.tcp://default@172.17.0.7:2552/system/IO-DNS/async-dns/$a] Attempting to resolve _management._tcp.integration-test-kubernetes-dns-internal.akka-bootstrap-demo-ns.svc.cluster.local with Actor[akka://default/system/IO-DNS/async-dns/$a/$a#1272991285]
[DEBUG] [12/13/2018 07:13:43.049] [default-akka.actor.default-dispatcher-18] [akka.tcp://default@172.17.0.7:2552/system/IO-TCP/selectors/$a/0] Successfully bound to /0.0.0.0:8558
[DEBUG] [12/13/2018 07:13:43.134] [default-akka.actor.default-dispatcher-18] [akka.tcp://default@172.17.0.7:2552/system/IO-DNS/async-dns/$a/$a] Resolving [_management._tcp.integration-test-kubernetes-dns-internal.akka-bootstrap-demo-ns.svc.cluster.local] (SRV)
[INFO] [12/13/2018 07:13:43.147] [default-akka.actor.default-dispatcher-6] [AkkaManagement(akka://default)] Bound Akka Management (HTTP) endpoint to: 0.0.0.0:8558
[DEBUG] [12/13/2018 07:13:43.156] [default-akka.actor.default-dispatcher-5] [akka.tcp://default@172.17.0.7:2552/system/IO-TCP/selectors/$a/1] Successfully bound to /0.0.0.0:8080
[INFO] [12/13/2018 07:13:43.180] [main] [akka.actor.ActorSystemImpl(default)] Server online at http://localhost:8080/
....
[INFO] [12/13/2018 07:13:50.631] [default-akka.actor.default-dispatcher-5] [akka.cluster.Cluster(akka://default)] Cluster Node [akka.tcp://default@172.17.0.7:2552] - Welcome from [akka.tcp://default@172.17.0.6:2552]
[DEBUG] [12/13/2018 07:13:50.644] [default-akka.remote.default-remote-dispatcher-22] [akka.serialization.Serialization(akka://default)] Using serializer [akka.cluster.protobuf.ClusterMessageSerializer] for message [akka.cluster.GossipEnvelope]
[INFO] [12/13/2018 07:13:50.659] [default-akka.actor.default-dispatcher-18] [akka.tcp://default@172.17.0.7:2552/user/$b] Cluster akka.tcp://default@172.17.0.7:2552 >>> MemberUp(Member(address = akka.tcp://default@172.17.0.6:2552, status = Up))
[INFO] [12/13/2018 07:13:50.676] [default-akka.actor.default-dispatcher-20] [akka.tcp://default@172.17.0.7:2552/user/$b] Cluster akka.tcp://default@172.17.0.7:2552 >>> MemberJoined(Member(address = akka.tcp://default@172.17.0.7:2552, status = Joining))
[INFO] [12/13/2018 07:13:50.716] [default-akka.actor.default-dispatcher-6] [akka.tcp://default@172.17.0.7:2552/user/$b] Cluster akka.tcp://default@172.17.0.7:2552 >>> LeaderChanged(Some(akka.tcp://default@172.17.0.6:2552))
[INFO] [12/13/2018 07:13:50.720] [default-akka.actor.default-dispatcher-3] [akka.tcp://default@172.17.0.7:2552/user/$b] Cluster akka.tcp://default@172.17.0.7:2552 >>> RoleLeaderChanged(dc-default,Some(akka.tcp://default@172.17.0.6:2552))
[INFO] [12/13/2018 07:13:50.727] [default-akka.actor.default-dispatcher-6] [akka.tcp://default@172.17.0.7:2552/user/$b] Cluster akka.tcp://default@172.17.0.7:2552 >>> SeenChanged(true,Set(akka.tcp://default@172.17.0.6:2552, akka.tcp://default@172.17.0.7:2552))
[INFO] [12/13/2018 07:13:50.733] [default-akka.actor.default-dispatcher-5] [akka.tcp://default@172.17.0.7:2552/user/$b] Cluster akka.tcp://default@172.17.0.7:2552 >>> ReachabilityChanged()
```
The source code for this page can be found here.