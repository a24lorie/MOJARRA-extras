Starting cache on console:
	java -cp COHERENCE_HOME\config;COHERENCE_HOME\lib\coherence.jar com.tangosol.net.DefaultCacheServer
	
Verifing cache
	java -cp COHERENCE_HOME\config;COHERENCE_HOME\lib\coherence.jar -Dtangosol.coherence.distributed.localstorage=false com.tangosol.net.CacheFactory

	command: cache (cache-name)
	command: list
	command: get (entry Id)
	
Tomcat start params
	...
	-Djava.util.logging.config.file=(path to)\JSF_Login.properties
	-XX:MaxPermSize=512m
	-XX:+UnlockCommercialFeatures 
	-XX:+FlightRecorder
	-Dcom.sun.management.jmxremote 
	-Dcom.sun.management.jmxremote.ssl=false 
	-Dcom.sun.management.jmxremote.authenticate=false 
	-Dcom.sun.management.jmxremote.port=7091
	-Dcom.sun.management.jmxremote.autodiscovery=true
	-Dtangosol.coherence.session.localstorage=false 
	-Dtangosol.coherence.distributed.localstorage=false 
	-Dtangosol.coherence.cluster=(Cluster Name configured on tangosol-coherence-override.xml)
	-Dtangosol.coherence.clusterport=(Cluster port configured on tangosol-coherence-override.xml)
	-Dtangosol.coherence.clusteraddress=(Addres configured on tangosol-coherence-override.xml)
	-Dtangosol.coherence.member=Tomcat
	-Dtangosol.coherence.cacheconfig=(path to)\exampleCache-config.xml 