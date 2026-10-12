import SwiftUI
import Shared

@main
struct EquallApp: App {
    var body: some Scene {
        WindowGroup {
            EquallView().ignoresSafeArea()
        }
    }
}

struct EquallView: UIViewControllerRepresentable {
    func makeUIViewController(context: Context) -> UIViewController {
        MainViewControllerKt.MainViewController()
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}
}
