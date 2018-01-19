/*
 * generated by Xtext 2.13.0
 */
package io.sapl.grammar

import com.google.inject.Injector
import io.sapl.grammar.sapl.SaplPackage
import org.eclipse.emf.ecore.EPackage

/**
 * Initialization support for running Xtext languages without Equinox extension registry.
 */
class SAPLStandaloneSetup extends SAPLStandaloneSetupGenerated {

	def static void doSetup() {
		new SAPLStandaloneSetup().createInjectorAndDoEMFRegistration()
	}
	
    override register(Injector injector) {
        if (!EPackage.Registry.INSTANCE.containsKey(SaplPackage.eNS_URI)) {
            EPackage.Registry.INSTANCE.put(SaplPackage.eNS_URI, 
            			SaplPackage.eINSTANCE);
        }
        super.register(injector);
    }
}