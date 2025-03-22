# IdolSnap 📸🎶

**A K-Pop fan-driven platform for sharing artist photos.**

IdolSnap was an Android application that allowed K-Pop fans to upload and share images of their favorite artists. The app leveraged machine learning and cloud services to enhance user experience, including Google Cloud Vision AI for content moderation and Firebase for backend functionality. Launched in 2021, the platform grew to over 1,000 users worldwide and was archived in 2023.

## 🔧 Tech Stack

### Frontend (Mobile Application)

- **Android (Java)** – Native Android app developed in Java using Android Studio.
- **Android Jetpack Components** – Utilized ViewModel, LiveData, and Room Database for UI and data management.
- **Glide** – Image loading and caching for optimized performance.

### Backend & Cloud Services

- **Firebase Firestore (NoSQL Database)** – Stored user-uploaded images and metadata.
- **Firebase Cloud Functions** (Node.js) – Handled image processing and automated backend operations.
- **Firebase Authentication** – Enabled user login and secure authentication.
- **Firebase Cloud Messaging (FCM)** – Sent push notifications to users.

### Machine Learning & Content Moderation

- **Google Cloud Vision AI** – Used to analyze and moderate uploaded images by detecting inappropriate or offensive content.

### Storage & Content Delivery

- Google Cloud Storage – Managed and stored user-uploaded images.
- Cloudflare CDN – Provided faster load times, reduced latency, and lower bandwidth costs by caching static content (such as images) at edge locations globally, ensuring users from different regions experienced minimal delay when accessing media.
  - Performance Benefits: Improved response times by delivering cached images from the closest CDN node.
  - Scalability: Handled high traffic loads efficiently by offloading requests from the primary storage.
  - Security: Protected against DDoS attacks and provided secure data transmission via HTTPS.

## 📂 Repository Structure

- app/ – Main Android application source code (activities, fragments, resources).
- gradle/ – Build and dependency management configurations.
- .safedk/ – SafeDK integration files.
- .idea/ – Android Studio project settings.

## 🚀 Getting Started

1. Clone the repository:

    ```bash
    git clone https://github.com/danielkim-im/idolSnap.git
    ```

2. Open the project in Android Studio.
3. Ensure all dependencies are installed and build the project.
4. Deploy the app to an emulator or physical Android device.

## 📞 Contact

For more information, visit the project's website.

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

_Note: This project was archived in 2023 and is no longer actively maintained. Some dependencies or services may require updates for proper functionality._
