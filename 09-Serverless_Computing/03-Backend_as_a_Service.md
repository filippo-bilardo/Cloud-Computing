# Backend as a Service (BaaS)

## Introduzione a BaaS

**Backend as a Service** (BaaS) fornisce servizi backend completi (database, auth, storage, API) come servizi gestiti, permettendo agli sviluppatori di concentrarsi sul frontend.

### BaaS vs FaaS

```
┌─────────────────────────────────────────────┐
│              BaaS                           │
├─────────────┬─────────────┬─────────────────┤
│ Database    │ Auth        │ Storage         │
│ (gestito)   │ (gestito)   │ (gestito)       │
└─────────────┴─────────────┴─────────────────┘
        ▲                    ▲
        │                    │
┌───────┴────────────────────┴─────────┐
│        Frontend (Web/Mobile)         │
└──────────────────────────────────────┘

┌─────────────────────────────────────────────┐
│              FaaS                           │
├─────────────────────────────────────────────┤
│    Custom Functions (developer code)        │
└─────────────────────────────────────────────┘
```

**BaaS**: Servizi completi pronti all'uso (Firebase, Supabase)
**FaaS**: Codice custom event-driven (Lambda, Cloud Functions)

---

## Firebase

**Firebase** di Google è la piattaforma BaaS più popolare per app mobile e web.

### Firebase Products

```
┌──────────────────────────────────────────────────┐
│               Firebase Platform                  │
├──────────────┬─────────────┬─────────────────────┤
│  Database    │  Auth       │  Storage            │
│  - Firestore │  - Email    │  - Cloud Storage    │
│  - Realtime  │  - Google   │                     │
│    DB        │  - Facebook │  Functions          │
├──────────────┼─────────────┼─────────────────────┤
│  Hosting     │ Analytics   │  Crashlytics        │
│  ML Kit      │ A/B Testing │  Remote Config      │
└──────────────┴─────────────┴─────────────────────┘
```

### Firebase Firestore

**Cloud Firestore**: Database NoSQL real-time, scalabile.

```javascript
// Initialize Firebase
import { initializeApp } from 'firebase/app';
import { getFirestore, collection, addDoc, getDocs, query, where, onSnapshot } from 'firebase/firestore';

const firebaseConfig = {
  apiKey: "AIza...",
  authDomain: "myapp.firebaseapp.com",
  projectId: "myapp",
  storageBucket: "myapp.appspot.com",
  messagingSenderId: "123456789",
  appId: "1:123456789:web:abc123"
};

const app = initializeApp(firebaseConfig);
const db = getFirestore(app);

// CREATE: Aggiungere documento
async function createUser(userData) {
  try {
    const docRef = await addDoc(collection(db, 'users'), {
      name: userData.name,
      email: userData.email,
      createdAt: new Date(),
      active: true
    });
    console.log('User created with ID:', docRef.id);
    return docRef.id;
  } catch (error) {
    console.error('Error adding document:', error);
  }
}

// READ: Leggere documenti
async function getUsers() {
  const querySnapshot = await getDocs(collection(db, 'users'));
  const users = [];
  querySnapshot.forEach((doc) => {
    users.push({
      id: doc.id,
      ...doc.data()
    });
  });
  return users;
}

// QUERY: Filtrare dati
async function getActiveUsers() {
  const q = query(
    collection(db, 'users'),
    where('active', '==', true),
    where('createdAt', '>', new Date('2024-01-01'))
  );
  
  const querySnapshot = await getDocs(q);
  return querySnapshot.docs.map(doc => ({ id: doc.id, ...doc.data() }));
}

// REAL-TIME: Listener per aggiornamenti
function listenToUsers(callback) {
  const unsubscribe = onSnapshot(collection(db, 'users'), (snapshot) => {
    const users = [];
    snapshot.forEach((doc) => {
      users.push({ id: doc.id, ...doc.data() });
    });
    callback(users);
  });
  
  // Call unsubscribe() to stop listening
  return unsubscribe;
}

// Uso
const unsubscribe = listenToUsers((users) => {
  console.log('Users updated:', users);
  // Update UI
});

// Later: stop listening
// unsubscribe();
```

### Firebase Authentication

```javascript
import { getAuth, createUserWithEmailAndPassword, signInWithEmailAndPassword, signInWithPopup, GoogleAuthProvider, onAuthStateChanged } from 'firebase/auth';

const auth = getAuth(app);

// Email/Password Signup
async function signup(email, password) {
  try {
    const userCredential = await createUserWithEmailAndPassword(auth, email, password);
    const user = userCredential.user;
    console.log('User signed up:', user.uid);
    return user;
  } catch (error) {
    console.error('Signup error:', error.code, error.message);
  }
}

// Email/Password Login
async function login(email, password) {
  try {
    const userCredential = await signInWithEmailAndPassword(auth, email, password);
    return userCredential.user;
  } catch (error) {
    console.error('Login error:', error.code, error.message);
  }
}

// Google Sign-In
async function signInWithGoogle() {
  const provider = new GoogleAuthProvider();
  try {
    const result = await signInWithPopup(auth, provider);
    const user = result.user;
    const credential = GoogleAuthProvider.credentialFromResult(result);
    const token = credential.accessToken;
    
    console.log('Signed in with Google:', user.displayName);
    return user;
  } catch (error) {
    console.error('Google sign-in error:', error);
  }
}

// Auth State Observer
onAuthStateChanged(auth, (user) => {
  if (user) {
    console.log('User is signed in:', user.uid);
    // User is signed in, update UI
  } else {
    console.log('User is signed out');
    // User is signed out, show login
  }
});

// Logout
async function logout() {
  await auth.signOut();
}
```

### Firebase Storage

```javascript
import { getStorage, ref, uploadBytes, getDownloadURL, deleteObject } from 'firebase/storage';

const storage = getStorage(app);

// Upload file
async function uploadFile(file) {
  const storageRef = ref(storage, `images/${file.name}`);
  
  try {
    const snapshot = await uploadBytes(storageRef, file);
    console.log('Uploaded:', snapshot.metadata.fullPath);
    
    // Get download URL
    const url = await getDownloadURL(snapshot.ref);
    console.log('File available at:', url);
    
    return url;
  } catch (error) {
    console.error('Upload error:', error);
  }
}

// Upload with metadata
async function uploadWithMetadata(file, userId) {
  const metadata = {
    contentType: file.type,
    customMetadata: {
      uploadedBy: userId,
      uploadedAt: new Date().toISOString()
    }
  };
  
  const storageRef = ref(storage, `uploads/${userId}/${file.name}`);
  await uploadBytes(storageRef, file, metadata);
  
  return await getDownloadURL(storageRef);
}

// Delete file
async function deleteFile(path) {
  const fileRef = ref(storage, path);
  await deleteObject(fileRef);
  console.log('File deleted');
}
```

### Firebase Cloud Functions

```javascript
// functions/index.js
const functions = require('firebase-functions');
const admin = require('firebase-admin');

admin.initializeApp();

// Trigger on user creation
exports.onUserCreated = functions.auth.user().onCreate(async (user) => {
  console.log('New user created:', user.uid);
  
  // Create user profile in Firestore
  await admin.firestore().collection('users').doc(user.uid).set({
    email: user.email,
    displayName: user.displayName || 'Anonymous',
    createdAt: admin.firestore.FieldValue.serverTimestamp(),
    role: 'user'
  });
  
  // Send welcome email
  // await sendWelcomeEmail(user.email);
});

// Trigger on Firestore write
exports.onOrderCreated = functions.firestore
  .document('orders/{orderId}')
  .onCreate(async (snap, context) => {
    const order = snap.data();
    const orderId = context.params.orderId;
    
    console.log('New order:', orderId, order);
    
    // Send notification
    const userId = order.userId;
    const userDoc = await admin.firestore().collection('users').doc(userId).get();
    const userEmail = userDoc.data().email;
    
    // Send confirmation email
    // await sendOrderConfirmation(userEmail, order);
    
    // Update analytics
    await admin.firestore().collection('analytics').doc('orders').update({
      totalOrders: admin.firestore.FieldValue.increment(1),
      totalRevenue: admin.firestore.FieldValue.increment(order.amount)
    });
  });

// HTTP Callable function
exports.processPayment = functions.https.onCall(async (data, context) => {
  // Verify authentication
  if (!context.auth) {
    throw new functions.https.HttpsError('unauthenticated', 'User must be authenticated');
  }
  
  const { orderId, amount } = data;
  
  // Process payment
  try {
    const result = await processStripePayment(amount);
    
    // Update order
    await admin.firestore().collection('orders').doc(orderId).update({
      status: 'paid',
      paymentId: result.id,
      paidAt: admin.firestore.FieldValue.serverTimestamp()
    });
    
    return { success: true, paymentId: result.id };
  } catch (error) {
    throw new functions.https.HttpsError('internal', error.message);
  }
});

// Scheduled function (cron)
exports.dailyCleanup = functions.pubsub.schedule('0 2 * * *')  // 2 AM daily
  .timeZone('Europe/Rome')
  .onRun(async (context) => {
    console.log('Running daily cleanup');
    
    // Delete old logs
    const cutoff = new Date();
    cutoff.setDate(cutoff.getDate() - 30);
    
    const snapshot = await admin.firestore()
      .collection('logs')
      .where('createdAt', '<', cutoff)
      .get();
    
    const batch = admin.firestore().batch();
    snapshot.docs.forEach((doc) => {
      batch.delete(doc.ref);
    });
    
    await batch.commit();
    console.log(`Deleted ${snapshot.size} old logs`);
  });
```

### Firestore Security Rules

```javascript
// firestore.rules
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    
    // Users can only read/write their own profile
    match /users/{userId} {
      allow read: if request.auth != null;
      allow write: if request.auth != null && request.auth.uid == userId;
    }
    
    // Public read, authenticated write
    match /posts/{postId} {
      allow read: if true;
      allow create: if request.auth != null;
      allow update, delete: if request.auth != null && 
        request.auth.uid == resource.data.authorId;
    }
    
    // Admin only
    match /admin/{document=**} {
      allow read, write: if request.auth != null && 
        get(/databases/$(database)/documents/users/$(request.auth.uid)).data.role == 'admin';
    }
    
    // Custom validation
    match /orders/{orderId} {
      allow create: if request.auth != null &&
        request.resource.data.userId == request.auth.uid &&
        request.resource.data.amount > 0 &&
        request.resource.data.items is list &&
        request.resource.data.items.size() > 0;
    }
  }
}
```

---

## AWS Amplify

**AWS Amplify** è la piattaforma BaaS di AWS per app mobile e web.

### Amplify Setup

```bash
# Install CLI
npm install -g @aws-amplify/cli

# Configure
amplify configure

# Initialize project
amplify init

# Add authentication
amplify add auth

# Add API (GraphQL or REST)
amplify add api

# Add storage
amplify add storage

# Deploy
amplify push
```

### Amplify with React

```javascript
// App.js
import { Amplify } from 'aws-amplify';
import { withAuthenticator } from '@aws-amplify/ui-react';
import '@aws-amplify/ui-react/styles.css';
import awsconfig from './aws-exports';

Amplify.configure(awsconfig);

function App({ signOut, user }) {
  return (
    <div>
      <h1>Hello {user.username}</h1>
      <button onClick={signOut}>Sign Out</button>
    </div>
  );
}

export default withAuthenticator(App);
```

### Amplify GraphQL API

```graphql
# schema.graphql
type Todo @model @auth(rules: [{ allow: owner }]) {
  id: ID!
  name: String!
  description: String
  completed: Boolean
  owner: String
}

type Note @model {
  id: ID!
  title: String!
  content: String!
  createdAt: AWSDateTime
}
```

```javascript
// Using the API
import { API } from 'aws-amplify';
import { createTodo, updateTodo, deleteTodo } from './graphql/mutations';
import { listTodos } from './graphql/queries';
import { onCreateTodo } from './graphql/subscriptions';

// Create
async function addTodo(name, description) {
  const todo = {
    name,
    description,
    completed: false
  };
  
  const result = await API.graphql({
    query: createTodo,
    variables: { input: todo }
  });
  
  return result.data.createTodo;
}

// Read
async function getTodos() {
  const result = await API.graphql({
    query: listTodos
  });
  
  return result.data.listTodos.items;
}

// Update
async function completeTodo(id) {
  const result = await API.graphql({
    query: updateTodo,
    variables: {
      input: {
        id,
        completed: true
      }
    }
  });
  
  return result.data.updateTodo;
}

// Delete
async function removeTodo(id) {
  await API.graphql({
    query: deleteTodo,
    variables: { input: { id } }
  });
}

// Real-time subscription
function subscribeToNewTodos(callback) {
  const subscription = API.graphql({
    query: onCreateTodo
  }).subscribe({
    next: ({ value }) => {
      callback(value.data.onCreateTodo);
    },
    error: (error) => console.error(error)
  });
  
  return subscription;
}

// Usage
const subscription = subscribeToNewTodos((todo) => {
  console.log('New todo:', todo);
});

// Cleanup
subscription.unsubscribe();
```

### Amplify Storage

```javascript
import { Storage } from 'aws-amplify';

// Upload file
async function uploadFile(file) {
  try {
    const result = await Storage.put(file.name, file, {
      contentType: file.type,
      level: 'private',  // 'public', 'protected', or 'private'
      metadata: {
        uploadedBy: 'user123',
        uploadedAt: new Date().toISOString()
      }
    });
    console.log('File uploaded:', result.key);
    return result;
  } catch (error) {
    console.error('Upload error:', error);
  }
}

// Get file URL
async function getFileUrl(key) {
  const url = await Storage.get(key, { level: 'private' });
  return url;
}

// List files
async function listFiles() {
  const result = await Storage.list('', { level: 'private' });
  return result;
}

// Delete file
async function deleteFile(key) {
  await Storage.remove(key, { level: 'private' });
}
```

---

## Supabase

**Supabase** è l'alternativa open-source a Firebase, basata su PostgreSQL.

### Supabase Features

- **Database**: PostgreSQL con REST API auto-generata
- **Auth**: Email, OAuth, magic links
- **Storage**: S3-compatible object storage
- **Real-time**: WebSocket subscriptions
- **Edge Functions**: Deno-based serverless functions

### Supabase Client

```javascript
import { createClient } from '@supabase/supabase-js';

const supabaseUrl = 'https://your-project.supabase.co';
const supabaseKey = 'your-anon-key';
const supabase = createClient(supabaseUrl, supabaseKey);

// INSERT
async function createPost(title, content) {
  const { data, error } = await supabase
    .from('posts')
    .insert([
      {
        title,
        content,
        user_id: supabase.auth.user().id,
        created_at: new Date()
      }
    ])
    .select();
  
  if (error) throw error;
  return data[0];
}

// SELECT
async function getPosts() {
  const { data, error } = await supabase
    .from('posts')
    .select('*, author:users(name, email)')
    .order('created_at', { ascending: false })
    .limit(10);
  
  if (error) throw error;
  return data;
}

// UPDATE
async function updatePost(id, updates) {
  const { data, error } = await supabase
    .from('posts')
    .update(updates)
    .eq('id', id)
    .select();
  
  if (error) throw error;
  return data[0];
}

// DELETE
async function deletePost(id) {
  const { error } = await supabase
    .from('posts')
    .delete()
    .eq('id', id);
  
  if (error) throw error;
}

// FILTER
async function getPublishedPosts() {
  const { data, error } = await supabase
    .from('posts')
    .select('*')
    .eq('published', true)
    .gte('created_at', '2024-01-01')
    .ilike('title', '%serverless%');
  
  if (error) throw error;
  return data;
}
```

### Supabase Authentication

```javascript
// Sign up
async function signUp(email, password) {
  const { data, error } = await supabase.auth.signUp({
    email,
    password
  });
  
  if (error) throw error;
  return data.user;
}

// Sign in
async function signIn(email, password) {
  const { data, error } = await supabase.auth.signInWithPassword({
    email,
    password
  });
  
  if (error) throw error;
  return data.user;
}

// OAuth (Google)
async function signInWithGoogle() {
  const { data, error } = await supabase.auth.signInWithOAuth({
    provider: 'google',
    options: {
      redirectTo: 'https://myapp.com/auth/callback'
    }
  });
  
  if (error) throw error;
}

// Sign out
async function signOut() {
  const { error } = await supabase.auth.signOut();
  if (error) throw error;
}

// Get current user
const user = supabase.auth.getUser();

// Listen to auth changes
supabase.auth.onAuthStateChange((event, session) => {
  console.log('Auth event:', event);
  if (event === 'SIGNED_IN') {
    console.log('User signed in:', session.user);
  }
});
```

### Supabase Real-time

```javascript
// Subscribe to changes
const subscription = supabase
  .channel('posts')
  .on('postgres_changes', {
    event: '*',  // INSERT, UPDATE, DELETE, or *
    schema: 'public',
    table: 'posts'
  }, (payload) => {
    console.log('Change received!', payload);
    // payload.eventType: INSERT, UPDATE, DELETE
    // payload.new: new record (INSERT/UPDATE)
    // payload.old: old record (UPDATE/DELETE)
  })
  .subscribe();

// Unsubscribe
subscription.unsubscribe();

// Presence (user online status)
const channel = supabase.channel('room1');

channel
  .on('presence', { event: 'sync' }, () => {
    const state = channel.presenceState();
    console.log('Online users:', state);
  })
  .subscribe(async (status) => {
    if (status === 'SUBSCRIBED') {
      await channel.track({ user_id: 'user123', online_at: new Date() });
    }
  });
```

### Supabase Row Level Security (RLS)

```sql
-- Enable RLS
ALTER TABLE posts ENABLE ROW LEVEL SECURITY;

-- Users can read all posts
CREATE POLICY "Posts are viewable by everyone"
ON posts FOR SELECT
USING (true);

-- Users can insert their own posts
CREATE POLICY "Users can create posts"
ON posts FOR INSERT
WITH CHECK (auth.uid() = user_id);

-- Users can update their own posts
CREATE POLICY "Users can update own posts"
ON posts FOR UPDATE
USING (auth.uid() = user_id)
WITH CHECK (auth.uid() = user_id);

-- Users can delete their own posts
CREATE POLICY "Users can delete own posts"
ON posts FOR DELETE
USING (auth.uid() = user_id);

-- Admin can do everything
CREATE POLICY "Admins have full access"
ON posts
USING (
  EXISTS (
    SELECT 1 FROM users
    WHERE users.id = auth.uid()
    AND users.role = 'admin'
  )
);
```

---

## Altri BaaS Providers

### 1. **Parse Server** (Open Source)

```javascript
const Parse = require('parse/node');

Parse.initialize("APP_ID", "JS_KEY");
Parse.serverURL = 'https://parseapi.back4app.com/';

// Create object
const GameScore = Parse.Object.extend("GameScore");
const gameScore = new GameScore();

gameScore.set("score", 1337);
gameScore.set("playerName", "John");

await gameScore.save();
```

### 2. **Appwrite** (Open Source)

```javascript
import { Client, Databases, Account } from 'appwrite';

const client = new Client()
  .setEndpoint('https://cloud.appwrite.io/v1')
  .setProject('PROJECT_ID');

const databases = new Databases(client);

// Create document
await databases.createDocument(
  'DATABASE_ID',
  'COLLECTION_ID',
  'unique()',
  { title: 'Hello', content: 'World' }
);
```

### 3. **Hasura** (GraphQL)

```graphql
# Auto-generated GraphQL from PostgreSQL

mutation CreateUser {
  insert_users_one(object: {
    name: "John",
    email: "john@example.com"
  }) {
    id
    name
    created_at
  }
}

query GetUsers {
  users(where: {active: {_eq: true}}) {
    id
    name
    posts {
      title
      created_at
    }
  }
}
```

---

## Comparazione BaaS Providers

| Feature | Firebase | AWS Amplify | Supabase | Appwrite |
|---------|----------|-------------|----------|----------|
| **Database** | NoSQL (Firestore) | DynamoDB + GraphQL | PostgreSQL | Multiple |
| **Auth** | Email, OAuth, Phone | Cognito | Email, OAuth | Email, OAuth |
| **Storage** | Cloud Storage | S3 | S3-compatible | Built-in |
| **Functions** | Cloud Functions | Lambda | Edge Functions | Cloud Functions |
| **Real-time** | ✅ | ✅ (AppSync) | ✅ | ✅ |
| **Pricing** | Free tier + usage | Free tier + usage | Free tier generous | Free (self-hosted) |
| **Open Source** | ❌ | ❌ | ✅ | ✅ |

---

## Best Practices BaaS

### 1. Security Rules

```javascript
// ✅ GOOD - Granular rules
allow read: if request.auth != null && 
  request.auth.uid == resource.data.userId;

// ❌ BAD - Too permissive
allow read, write: if true;
```

### 2. Indexing

```javascript
// Firestore composite index
// firebase.json
{
  "firestore": {
    "indexes": [
      {
        "collectionGroup": "posts",
        "queryScope": "COLLECTION",
        "fields": [
          { "fieldPath": "published", "order": "ASCENDING" },
          { "fieldPath": "createdAt", "order": "DESCENDING" }
        ]
      }
    ]
  }
}
```

### 3. Batch Operations

```javascript
// ✅ GOOD - Batch write
const batch = db.batch();
posts.forEach(post => {
  const ref = db.collection('posts').doc();
  batch.set(ref, post);
});
await batch.commit();

// ❌ BAD - Individual writes
for (const post of posts) {
  await db.collection('posts').add(post);  // Slow!
}
```

### 4. Offline Support

```javascript
// Firebase offline persistence
import { enableIndexedDbPersistence } from 'firebase/firestore';

enableIndexedDbPersistence(db)
  .catch((err) => {
    if (err.code == 'failed-precondition') {
      console.log('Multiple tabs open');
    } else if (err.code == 'unimplemented') {
      console.log('Browser not supported');
    }
  });
```

---

## Esercizi

1. **Todo App**: Firebase Firestore + Auth + real-time sync
2. **Blog Platform**: Supabase con RLS, auth, storage
3. **E-commerce**: AWS Amplify GraphQL API + Cognito
4. **Chat App**: Firebase real-time database + presence
5. **File Manager**: Storage + thumbnails + metadata
6. **Multi-tenant SaaS**: Row Level Security patterns

---

## Domande di Verifica

1. Qual è la differenza tra BaaS e FaaS?
2. Come funziona l'autenticazione in Firebase?
3. Cosa sono le Security Rules in Firestore?
4. Come implementi real-time sync con Supabase?
5. Quali sono i vantaggi di Supabase rispetto a Firebase?
6. Come gestisci file upload in AWS Amplify?
7. Cosa sono le GraphQL subscriptions?
8. Come implementi Row Level Security in PostgreSQL/Supabase?
9. Quando preferire BaaS rispetto a custom backend?
10. Come ottimizzi le query in Firestore?

---

## Risorse Aggiuntive

- [Firebase Documentation](https://firebase.google.com/docs)
- [AWS Amplify Docs](https://docs.amplify.aws/)
- [Supabase Documentation](https://supabase.com/docs)
- [Appwrite Docs](https://appwrite.io/docs)
- [Parse Server](https://parseplatform.org/)
- [Hasura](https://hasura.io/docs/)
