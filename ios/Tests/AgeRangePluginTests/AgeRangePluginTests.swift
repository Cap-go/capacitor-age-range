import XCTest
@testable import AgeRangePlugin

class AgeRangePluginTests: XCTestCase {
    func testAgeRangeImplExists() throws {
        let impl = AgeRangeImpl()
        XCTAssertNotNil(impl)
    }
}
