#!groovy

def decidepipeline(Map configMap){
   def application = configMap.get("application")
    switch(application) {
        case 'nodeJSVM':
            echo "application is NodeJS and VM based "
            //nodeJSVM(configMap)
            break
        case 'javaVM':
            javaVM(configMap)
            break
        default:
            error "Un recognised application: ${application}"
            break     
    }
}