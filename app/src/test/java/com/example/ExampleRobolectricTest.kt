package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Lovy Chat", appName)
  }

  @Test
  fun `verify E2EE AES-GCM encryption and decryption`() {
    val userA = "alice"
    val userB = "bob"
    val sharedKey = com.example.data.crypto.CryptoHelper.deriveSharedKey(userA, userB)
    val plainMessage = "Halo, pesan rahasia ini dienkripsi E2EE!"

    val encrypted = com.example.data.crypto.CryptoHelper.encrypt(plainMessage, sharedKey)
    assert(encrypted.cipherText.isNotEmpty())
    assert(encrypted.cipherText != plainMessage)

    val decrypted = com.example.data.crypto.CryptoHelper.decrypt(encrypted.cipherText, encrypted.iv, sharedKey)
    assertEquals(plainMessage, decrypted)
  }

  @Test
  fun `verify repository initializes with zero sample data`() {
    val repository = com.example.data.repository.LovyChatRepository()
    assertEquals(emptyList<com.example.data.model.Chat>(), repository.chats.value)
    assertEquals(emptyList<com.example.data.model.NearbyUser>(), repository.nearbyUsers.value)
    assertEquals(emptyList<com.example.data.model.GroupCommunity>(), repository.groups.value)
    assertEquals(emptyMap<String, List<com.example.data.model.Message>>(), repository.messages.value)
  }

  @Test
  fun `verify Google sign in and sign out lifecycle`() {
    val repository = com.example.data.repository.LovyChatRepository()
    assertEquals(null, repository.currentUser.value)

    repository.signInWithGoogleAccount(
      email = "boxsimokerto5@gmail.com",
      displayName = "Box Simokerto",
      photoUrl = null
    )

    val loggedInUser = repository.currentUser.value
    assert(loggedInUser != null)
    assertEquals("boxsimokerto5@gmail.com", loggedInUser?.email)
    assertEquals("Box Simokerto", loggedInUser?.name)

    repository.signOut()
    assertEquals(null, repository.currentUser.value)
  }

  @Test
  fun `verify Supabase manager loads and updates configuration`() {
    val manager = com.example.data.supabase.SupabaseManager.instance
    assert(manager.supabaseUrl.value.isNotEmpty())
    assert(manager.supabaseAnonKey.value.isNotEmpty())

    val testUrl = "https://custom-project.supabase.co"
    val testKey = "custom-anon-key-12345"
    manager.updateConfig(testUrl, testKey)

    assertEquals(testUrl, manager.supabaseUrl.value)
    assertEquals(testKey, manager.supabaseAnonKey.value)
    assertEquals(true, manager.isConnected.value)
  }
}
