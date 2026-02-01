import Foundation
import Capacitor

@objc(AgeRangePlugin)
public class AgeRangePlugin: CAPPlugin, CAPBridgedPlugin {
    private let pluginVersion: String = "8.1.0"
    public let identifier = "AgeRangePlugin"
    public let jsName = "AgeRange"
    public let pluginMethods: [CAPPluginMethod] = [
        CAPPluginMethod(name: "requestAgeRange", returnType: CAPPluginReturnPromise),
        CAPPluginMethod(name: "getPluginVersion", returnType: CAPPluginReturnPromise)
    ]

    private let implementation = AgeRangeImpl()

    @objc func requestAgeRange(_ call: CAPPluginCall) {
        let rawGates = call.getArray("ageGates") ?? [13, 16, 18]
        let ageGates = rawGates.compactMap { $0 as? Int }

        guard let viewController = self.bridge?.viewController else {
            call.reject("Unable to get view controller")
            return
        }

        implementation.requestAgeRange(ageGates: ageGates, viewController: viewController) { result in
            call.resolve(result)
        }
    }

    @objc func getPluginVersion(_ call: CAPPluginCall) {
        call.resolve(["version": self.pluginVersion])
    }
}
