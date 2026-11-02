import Shared
import SwiftUI
import UIKit

@main
struct iOSApp: App {
    @UIApplicationDelegateAdaptor(AppDelegate.self) private var appDelegate

    var body: some Scene {
        WindowGroup {
            ContentView()
                .onOpenURL { url in
                    SubmitDeepLinkKt.submitDeepLink(uri: url.absoluteString)
                }
                .onAppear {
                    appDelegate.submitPendingShortcut()
                }
        }
    }
}

final class AppDelegate: NSObject, UIApplicationDelegate {
    private var pendingShortcut: String?

    func application(
        _ application: UIApplication,
        configurationForConnecting connectingSceneSession: UISceneSession,
        options: UIScene.ConnectionOptions
    ) -> UISceneConfiguration {
        queue(options.shortcutItem)
        let configuration = UISceneConfiguration(name: nil, sessionRole: connectingSceneSession.role)
        if connectingSceneSession.role == .windowApplication {
            configuration.delegateClass = SceneDelegate.self
        }
        return configuration
    }

    func queue(_ item: UIApplicationShortcutItem?) {
        guard let url = item?.userInfo?["url"] as? String else { return }
        pendingShortcut = url
    }

    // Koin is not up until the Compose view exists, so a cold start waits for the UI.
    func submitPendingShortcut() {
        SubmitDeepLinkKt.submitDeepLink(uri: pendingShortcut)
        pendingShortcut = nil
    }

    // Fallback for the case where no scene delegate handles the action.
    func application(
        _ application: UIApplication,
        performActionFor shortcutItem: UIApplicationShortcutItem,
        completionHandler: @escaping (Bool) -> Void
    ) {
        SubmitDeepLinkKt.submitDeepLink(uri: shortcutItem.userInfo?["url"] as? String)
        completionHandler(true)
    }
}

final class SceneDelegate: NSObject, UIWindowSceneDelegate {
    // A cold start carries the quick action in the connection options.
    func scene(
        _ scene: UIScene,
        willConnectTo session: UISceneSession,
        options connectionOptions: UIScene.ConnectionOptions
    ) {
        appDelegate?.queue(connectionOptions.shortcutItem)
    }

    // With scenes, quick actions are delivered here and not to the app delegate.
    func windowScene(
        _ windowScene: UIWindowScene,
        performActionFor shortcutItem: UIApplicationShortcutItem,
        completionHandler: @escaping (Bool) -> Void
    ) {
        SubmitDeepLinkKt.submitDeepLink(uri: shortcutItem.userInfo?["url"] as? String)
        completionHandler(true)
    }

    private var appDelegate: AppDelegate? { UIApplication.shared.delegate as? AppDelegate }
}
