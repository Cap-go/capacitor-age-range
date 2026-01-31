import Foundation
import UIKit

#if canImport(DeclaredAgeRange)
import DeclaredAgeRange
#endif

class AgeRangeImpl {

    func requestAgeRange(ageGates: [Int], viewController: UIViewController, completion: @escaping ([String: Any]) -> Void) {
        #if canImport(DeclaredAgeRange)
        if #available(iOS 26, *) {
            requestAgeRangeImpl(ageGates: ageGates, viewController: viewController, completion: completion)
        } else {
            completion(["status": "NOT_AVAILABLE"])
        }
        #else
        completion(["status": "NOT_AVAILABLE"])
        #endif
    }

    #if canImport(DeclaredAgeRange)
    @available(iOS 26, *)
    private func requestAgeRangeImpl(ageGates: [Int], viewController: UIViewController, completion: @escaping ([String: Any]) -> Void) {
        let t1 = ageGates.first ?? 13
        let t2: Int? = ageGates.count > 1 ? ageGates[1] : nil
        let t3: Int? = ageGates.count > 2 ? ageGates[2] : nil

        Task { @MainActor in
            do {
                let response = try await AgeRangeService.shared.requestAgeRange(
                    ageGates: t1, t2, t3,
                    in: viewController
                )

                switch response {
                case .sharing(let range):
                    var result: [String: Any] = ["status": "SHARING"]
                    if let lower = range.lowerBound {
                        result["ageLower"] = lower
                    }
                    if let upper = range.upperBound {
                        result["ageUpper"] = upper
                    }

                    switch range.ageRangeDeclaration {
                    case .selfDeclared:
                        result["declarationSource"] = "SELF_DECLARED"
                    case .guardianDeclared:
                        result["declarationSource"] = "GUARDIAN_DECLARED"
                    case .none:
                        result["declarationSource"] = "UNKNOWN"
                    @unknown default:
                        result["declarationSource"] = "UNKNOWN"
                    }

                    completion(result)

                case .declinedSharing:
                    completion(["status": "DECLINED_SHARING"])

                @unknown default:
                    completion(["status": "DECLINED_SHARING"])
                }
            } catch {
                completion([
                    "status": "ERROR",
                    "error": error.localizedDescription
                ])
            }
        }
    }
    #endif
}
