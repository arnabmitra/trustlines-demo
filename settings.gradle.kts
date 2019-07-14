pluginManagement {
	repositories {
		gradlePluginPortal()
	}
}
rootProject.name = "trustlines-demo"
include ( "alices-server",
		"bobs-server",
		"trustlines-model","orchestrator","common-services","client")
