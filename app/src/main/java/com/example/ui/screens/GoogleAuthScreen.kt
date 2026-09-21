package com.example.ui.screens

import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import com.example.R
import com.example.data.supabase.SupabaseRepository
import com.example.ui.components.Tactile3DButton
import com.example.ui.components.TactileButtonType
import com.example.ui.theme.BlueAccent
import com.example.ui.theme.GrayLight
import com.example.ui.theme.GreenPrimary
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoogleAuthScreen(
    onGoogleSignInSuccess: (name: String, email: String, isNewUser: Boolean, phone: String, receiveUpdates: Boolean) -> Unit,
    onSkipOrGuest: () -> Unit = {},
    onBackClick: (() -> Unit)? = null,
    onNavigateToTerms: () -> Unit = {},
    modifier: Modifier = Modifier,
    initialTab: Int = 0,
    webClientId: String = "",
    supabaseUrl: String = "",
    supabaseAnonKey: String = ""
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val supabaseRepo = remember { SupabaseRepository() }

    // Tab state: 0 = DAFTAR AKUN BARU, 1 = MASUK
    var selectedTab by remember(initialTab) { mutableIntStateOf(initialTab) }

    // Register fields
    var regName by remember { mutableStateOf("") }
    var regEmail by remember { mutableStateOf("") }
    var regPhone by remember { mutableStateOf("") }
    var regPassword by remember { mutableStateOf("") }
    var regConfirmPassword by remember { mutableStateOf("") }
    var regPasswordVisible by remember { mutableStateOf(false) }
    var regConfirmPasswordVisible by remember { mutableStateOf(false) }
    var agreedToTerms by remember { mutableStateOf(false) }
    var receiveUpdates by remember { mutableStateOf(true) }

    // Login fields
    var loginEmail by remember { mutableStateOf("") }
    var loginPassword by remember { mutableStateOf("") }
    var loginPasswordVisible by remember { mutableStateOf(false) }

    var isAuthenticating by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // OTP Verification State
    var isVerifyingOtp by remember { mutableStateOf(false) }
    var otpCode by remember { mutableStateOf("") }
    var pendingEmail by remember { mutableStateOf("") }
    var pendingName by remember { mutableStateOf("") }
    var pendingPhone by remember { mutableStateOf("") }
    var pendingReceiveUpdates by remember { mutableStateOf(true) }
    var resendCooldown by remember { mutableIntStateOf(0) }

    LaunchedEffect(isVerifyingOtp, resendCooldown) {
        if (isVerifyingOtp && resendCooldown > 0) {
            delay(1000)
            resendCooldown -= 1
        }
    }

    fun triggerGoogleSignIn() {
        isAuthenticating = true
        errorMessage = null
        coroutineScope.launch {
            try {
                val credentialManager = CredentialManager.create(context)
                val clientId = if (webClientId.isNotBlank()) webClientId else "302781807275-b99k3ntgl0i3j0dj8rt286d4t55hhknd.apps.googleusercontent.com"
                val googleIdOption = GetGoogleIdOption.Builder()
                    .setFilterByAuthorizedAccounts(false)
                    .setServerClientId(clientId)
                    .setAutoSelectEnabled(false)
                    .build()

                val request = GetCredentialRequest.Builder()
                    .addCredentialOption(googleIdOption)
                    .build()

                val result = credentialManager.getCredential(context = context, request = request)
                val credential = result.credential

                if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                    val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                    val displayName = googleIdTokenCredential.displayName ?: googleIdTokenCredential.givenName ?: "Pengguna Google"
                    val email = googleIdTokenCredential.id
                    isAuthenticating = false
                    Toast.makeText(context, "Berhasil masuk dengan Google! ✨", Toast.LENGTH_SHORT).show()
                    onGoogleSignInSuccess(displayName, email, false, "", true)
                } else {
                    isAuthenticating = false
                    errorMessage = "Gagal memproses kredensial Google dari CredentialManager."
                }
            } catch (e: Exception) {
                isAuthenticating = false
                val msg = e.localizedMessage ?: e.message ?: ""
                errorMessage = if (msg.contains("No credential available", ignoreCase = true) || msg.contains("16", ignoreCase = true)) {
                    "Tidak ada akun Google yang tersinkron di perangkat ini. Silakan daftar menggunakan form di bawah."
                } else {
                    "Google Sign-In: $msg"
                }
            }
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            if (onBackClick != null) {
                TopAppBar(
                    title = {
                        Text(
                            text = if (selectedTab == 0) "Buat Akun Baru" else "Masuk ke Akun",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onBackClick) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Kembali"
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                )
            }
        }
    ) { innerPadding ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp, vertical = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header Logo & Brand
                Image(
                    painter = painterResource(id = R.drawable.ic_user_app_icon),
                    contentDescription = "Routina Logo",
                    modifier = Modifier
                        .size(64.dp)
                        .clip(RoundedCornerShape(16.dp))
                )
                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = if (selectedTab == 0) "Buat Akun Routina" else "Selamat Datang Kembali",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = if (selectedTab == 0)
                        "Daftar akun untuk menyimpan kebiasaan dan perkembangan kamu."
                    else
                        "Masuk untuk melanjutkan kebiasaan dan perkembangan kamu.",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )

                if (isVerifyingOtp) {
                    Spacer(modifier = Modifier.height(16.dp))

                    // --- TAMPILAN VERIFIKASI KODE EMAIL (OTP) ---
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(BlueAccent.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.MarkEmailRead,
                            contentDescription = null,
                            tint = BlueAccent,
                            modifier = Modifier.size(34.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Verifikasi Kode Email",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Masukkan kode verifikasi yang dikirim ke:",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Surface(
                        color = BlueAccent.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, BlueAccent.copy(alpha = 0.3f))
                    ) {
                        Text(
                            text = pendingEmail,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = BlueAccent,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Masukkan kode yang dikirim ke email kamu.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Error Message banner inside OTP
                    AnimatedVisibility(visible = errorMessage != null) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 14.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.error.copy(alpha = 0.12f))
                                .border(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                                .padding(12.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = errorMessage ?: "",
                                    color = MaterialTheme.colorScheme.error,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }

                    // OTP Input Field
                    OutlinedTextField(
                        value = otpCode,
                        onValueChange = { input ->
                            val cleaned = input.filter { it.isDigit() || it.isLetter() }.take(8)
                            otpCode = cleaned
                            errorMessage = null
                        },
                        label = { Text("Kode Verifikasi (OTP)") },
                        placeholder = { Text("Masukkan kode OTP", textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth()) },
                        textStyle = LocalTextStyle.current.copy(
                            textAlign = TextAlign.Center,
                            fontSize = if (otpCode.length > 6) 20.sp else 24.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = if (otpCode.length > 6) 4.sp else 6.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        ),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Tactile3DButton(
                        text = if (isAuthenticating) "Memverifikasi kode..." else "Verifikasi & Masuk",
                        enabled = !isAuthenticating && otpCode.length >= 6,
                        onClick = {
                            if (otpCode.length < 6) {
                                errorMessage = "Mohon masukkan kode verifikasi OTP."
                                return@Tactile3DButton
                            }
                            errorMessage = null
                            isAuthenticating = true
                            coroutineScope.launch {
                                val result = supabaseRepo.verifyOtp(
                                    email = pendingEmail,
                                    token = otpCode,
                                    customUrl = supabaseUrl,
                                    customAnonKey = supabaseAnonKey
                                )
                                isAuthenticating = false
                                if (result.isSuccess) {
                                    val displayName = result.name ?: pendingName
                                    Toast.makeText(context, "Verifikasi berhasil. Selamat datang, $displayName!", Toast.LENGTH_SHORT).show()
                                    onGoogleSignInSuccess(displayName, pendingEmail, true, pendingPhone, pendingReceiveUpdates)
                                } else {
                                    errorMessage = result.errorMessage ?: "Kode verifikasi salah atau kedaluwarsa."
                                }
                            }
                        },
                        type = TactileButtonType.PRIMARY
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Resend Code Row
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Tidak menerima kode? ",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (resendCooldown > 0) {
                            Text(
                                text = "Kirim ulang (${resendCooldown}s)",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = GrayLight
                            )
                        } else {
                            Text(
                                text = "Kirim Ulang",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = BlueAccent,
                                modifier = Modifier
                                    .clickable(enabled = !isAuthenticating) {
                                        errorMessage = null
                                        coroutineScope.launch {
                                            val res = supabaseRepo.resendOtp(pendingEmail, supabaseUrl, supabaseAnonKey)
                                            res.onSuccess { msg ->
                                                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                                resendCooldown = 60
                                            }.onFailure { e ->
                                                errorMessage = e.message ?: "Gagal mengirim ulang kode."
                                            }
                                        }
                                    }
                                    .padding(4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    TextButton(
                        onClick = {
                            isVerifyingOtp = false
                            errorMessage = null
                        }
                    ) {
                        Icon(Icons.Default.ArrowBack, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Kembali ke Form Pendaftaran", fontSize = 12.sp)
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                } else {
                    Spacer(modifier = Modifier.height(20.dp))

                    // Tab Switcher: DAFTAR vs MASUK
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                            .padding(4.dp)
                    ) {
                        // Tab Daftar
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (selectedTab == 0) GreenPrimary else Color.Transparent)
                                .clickable {
                                    selectedTab = 0
                                    errorMessage = null
                                }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Daftar Akun",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = if (selectedTab == 0) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        // Tab Masuk
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (selectedTab == 1) BlueAccent else Color.Transparent)
                                .clickable {
                                    selectedTab = 1
                                    errorMessage = null
                                }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Masuk",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = if (selectedTab == 1) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Error Message banner
                    AnimatedVisibility(visible = errorMessage != null) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 14.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.error.copy(alpha = 0.12f))
                                .border(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                                .padding(12.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = errorMessage ?: "",
                                    color = MaterialTheme.colorScheme.error,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }

                    // FORM CONTENT
                    if (selectedTab == 0) {
                        // --- FORM DAFTAR AKUN BARU ---
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            // Nama Lengkap
                            OutlinedTextField(
                                value = regName,
                                onValueChange = { regName = it },
                                label = { Text("Nama Lengkap") },
                                placeholder = { Text("Contoh: Budi Santoso") },
                                leadingIcon = {
                                    Icon(Icons.Default.Person, contentDescription = null, tint = GreenPrimary)
                                },
                                singleLine = true,
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier.fillMaxWidth()
                            )

                            // Email
                            OutlinedTextField(
                                value = regEmail,
                                onValueChange = { regEmail = it },
                                label = { Text("Email") },
                                placeholder = { Text("nama@email.com") },
                                leadingIcon = {
                                    Icon(Icons.Default.Email, contentDescription = null, tint = GreenPrimary)
                                },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                                singleLine = true,
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier.fillMaxWidth()
                            )

                            // Nomor WhatsApp / HP (Opsional)
                            OutlinedTextField(
                                value = regPhone,
                                onValueChange = { input ->
                                    if (input.all { it.isDigit() || it == '+' || it == ' ' || it == '-' }) {
                                        regPhone = input
                                    }
                                },
                                label = { Text("Nomor WhatsApp / HP (Opsional)") },
                                placeholder = { Text("Contoh: 081234567890") },
                                leadingIcon = {
                                    Icon(Icons.Default.Phone, contentDescription = null, tint = GreenPrimary)
                                },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                singleLine = true,
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier.fillMaxWidth()
                            )

                            // Kata Sandi
                            OutlinedTextField(
                                value = regPassword,
                                onValueChange = { regPassword = it },
                                label = { Text("Kata Sandi") },
                                placeholder = { Text("Minimal 6 karakter") },
                                leadingIcon = {
                                    Icon(Icons.Default.Lock, contentDescription = null, tint = GreenPrimary)
                                },
                                trailingIcon = {
                                    IconButton(onClick = { regPasswordVisible = !regPasswordVisible }) {
                                        Icon(
                                            imageVector = if (regPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                            contentDescription = if (regPasswordVisible) "Sembunyikan sandi" else "Tampilkan sandi"
                                        )
                                    }
                                },
                                visualTransformation = if (regPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                                singleLine = true,
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier.fillMaxWidth()
                            )

                            // Konfirmasi Kata Sandi
                            OutlinedTextField(
                                value = regConfirmPassword,
                                onValueChange = { regConfirmPassword = it },
                                label = { Text("Konfirmasi Kata Sandi") },
                                placeholder = { Text("Ulangi kata sandi") },
                                leadingIcon = {
                                    Icon(Icons.Default.Lock, contentDescription = null, tint = GreenPrimary)
                                },
                                trailingIcon = {
                                    IconButton(onClick = { regConfirmPasswordVisible = !regConfirmPasswordVisible }) {
                                        Icon(
                                            imageVector = if (regConfirmPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                            contentDescription = if (regConfirmPasswordVisible) "Sembunyikan sandi" else "Tampilkan sandi"
                                        )
                                    }
                                },
                                visualTransformation = if (regConfirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                                singleLine = true,
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier.fillMaxWidth()
                            )

                            // Terms and Conditions Agreement Row
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = agreedToTerms,
                                    onCheckedChange = { agreedToTerms = it },
                                    colors = CheckboxDefaults.colors(
                                        checkedColor = Color(0xFF1CB0F6),
                                        checkmarkColor = Color.White
                                    ),
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Saya menyetujui ",
                                        fontSize = 13.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "Syarat dan Ketentuan",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF1CB0F6),
                                        modifier = Modifier.clickable { onNavigateToTerms() }
                                    )
                                }
                            }

                            // Update Notification Checkbox Row
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { receiveUpdates = !receiveUpdates }
                                    .padding(vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = receiveUpdates,
                                    onCheckedChange = { receiveUpdates = it },
                                    colors = CheckboxDefaults.colors(
                                        checkedColor = GreenPrimary,
                                        checkmarkColor = Color.White
                                    ),
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Kirim info update fitur, info aplikasi, atau notifikasi ke email atau nomor WhatsApp saya",
                                    fontSize = 12.sp,
                                    lineHeight = 16.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f)
                                )
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            Tactile3DButton(
                                text = if (isAuthenticating) "Mendaftarkan..." else "Buat Akun",
                                enabled = !isAuthenticating,
                                onClick = {
                                    val nameTrimmed = regName.trim()
                                    val emailTrimmed = regEmail.trim()
                                    val phoneTrimmed = regPhone.trim()
                                    val passTrimmed = regPassword.trim()
                                    val confirmTrimmed = regConfirmPassword.trim()

                                    if (nameTrimmed.isBlank()) {
                                        errorMessage = "Mohon isi nama lengkap Anda."
                                        return@Tactile3DButton
                                    }
                                    if (emailTrimmed.isBlank() || !android.util.Patterns.EMAIL_ADDRESS.matcher(emailTrimmed).matches()) {
                                        errorMessage = "Mohon masukkan alamat email yang valid."
                                        return@Tactile3DButton
                                    }
                                    if (passTrimmed.length < 6) {
                                        errorMessage = "Kata sandi minimal harus 6 karakter."
                                        return@Tactile3DButton
                                    }
                                    if (passTrimmed != confirmTrimmed) {
                                        errorMessage = "Konfirmasi kata sandi tidak cocok."
                                        return@Tactile3DButton
                                    }
                                    if (!agreedToTerms) {
                                        errorMessage = "Mohon centang persetujuan Syarat dan Ketentuan terlebih dahulu."
                                        return@Tactile3DButton
                                    }

                                    errorMessage = null
                                    isAuthenticating = true
                                    coroutineScope.launch {
                                        val result = supabaseRepo.signUpWithEmail(
                                            email = emailTrimmed,
                                            password = passTrimmed,
                                            name = nameTrimmed,
                                            phone = phoneTrimmed,
                                            receiveUpdates = receiveUpdates,
                                            customUrl = supabaseUrl,
                                            customAnonKey = supabaseAnonKey
                                        )
                                        isAuthenticating = false
                                        if (result.isSuccess) {
                                            if (result.needsEmailVerification) {
                                                pendingEmail = emailTrimmed
                                                pendingName = result.name ?: nameTrimmed
                                                pendingPhone = phoneTrimmed
                                                pendingReceiveUpdates = receiveUpdates
                                                isVerifyingOtp = true
                                                otpCode = ""
                                                resendCooldown = 60
                                                Toast.makeText(context, "Kode verifikasi telah dikirim ke $emailTrimmed", Toast.LENGTH_LONG).show()
                                            } else {
                                                val displayName = result.name ?: nameTrimmed
                                                Toast.makeText(context, "Akun berhasil dibuat. Selamat datang, $displayName!", Toast.LENGTH_SHORT).show()
                                                onGoogleSignInSuccess(displayName, emailTrimmed, true, phoneTrimmed, receiveUpdates)
                                            }
                                        } else {
                                            errorMessage = result.errorMessage ?: "Gagal membuat akun."
                                        }
                                    }
                                },
                                type = TactileButtonType.PRIMARY
                            )
                        }
                    } else {
                        // --- FORM MASUK KE AKUN ---
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            // Email
                            OutlinedTextField(
                                value = loginEmail,
                                onValueChange = { loginEmail = it },
                                label = { Text("Email") },
                                placeholder = { Text("nama@email.com") },
                                leadingIcon = {
                                    Icon(Icons.Default.Email, contentDescription = null, tint = BlueAccent)
                                },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                                singleLine = true,
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier.fillMaxWidth()
                            )

                            // Kata Sandi
                            OutlinedTextField(
                                value = loginPassword,
                                onValueChange = { loginPassword = it },
                                label = { Text("Kata Sandi") },
                                placeholder = { Text("Masukkan kata sandi") },
                                leadingIcon = {
                                    Icon(Icons.Default.Lock, contentDescription = null, tint = BlueAccent)
                                },
                                trailingIcon = {
                                    IconButton(onClick = { loginPasswordVisible = !loginPasswordVisible }) {
                                        Icon(
                                            imageVector = if (loginPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                            contentDescription = if (loginPasswordVisible) "Sembunyikan sandi" else "Tampilkan sandi"
                                        )
                                    }
                                },
                                visualTransformation = if (loginPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                                singleLine = true,
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Tactile3DButton(
                                text = if (isAuthenticating) "Memverifikasi..." else "Masuk ke Akun",
                                enabled = !isAuthenticating,
                                onClick = {
                                    val emailTrimmed = loginEmail.trim()
                                    val passTrimmed = loginPassword.trim()

                                    if (emailTrimmed.isBlank() || !android.util.Patterns.EMAIL_ADDRESS.matcher(emailTrimmed).matches()) {
                                        errorMessage = "Mohon masukkan email yang valid."
                                        return@Tactile3DButton
                                    }
                                    if (passTrimmed.isBlank()) {
                                        errorMessage = "Mohon masukkan kata sandi Anda."
                                        return@Tactile3DButton
                                    }

                                    errorMessage = null
                                    isAuthenticating = true
                                    coroutineScope.launch {
                                        val result = supabaseRepo.signInWithEmail(
                                            email = emailTrimmed,
                                            password = passTrimmed,
                                            customUrl = supabaseUrl,
                                            customAnonKey = supabaseAnonKey
                                        )
                                        isAuthenticating = false
                                        if (result.isSuccess) {
                                            val displayName = result.name ?: emailTrimmed.substringBefore("@")
                                            Toast.makeText(context, "Selamat datang kembali, $displayName!", Toast.LENGTH_SHORT).show()
                                            onGoogleSignInSuccess(displayName, result.email ?: emailTrimmed, false, "", true)
                                        } else {
                                            val err = result.errorMessage ?: "Akun tidak ditemukan atau kata sandi salah."
                                            if (err.contains("Email belum dikonfirmasi", ignoreCase = true) || err.contains("Email not confirmed", ignoreCase = true)) {
                                                pendingEmail = emailTrimmed
                                                pendingName = emailTrimmed.substringBefore("@")
                                                isVerifyingOtp = true
                                                otpCode = ""
                                                resendCooldown = 60
                                                errorMessage = "Email belum diverifikasi. Masukkan kode verifikasi dari email untuk mengaktifkan akun."
                                                coroutineScope.launch {
                                                    supabaseRepo.resendOtp(emailTrimmed, supabaseUrl, supabaseAnonKey)
                                                }
                                            } else {
                                                errorMessage = err
                                            }
                                        }
                                    }
                                },
                                type = TactileButtonType.PRIMARY
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // DIVIDER: ATAU MASUK DENGAN GOOGLE
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        HorizontalDivider(modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                        Text(
                            text = "atau masuk instan",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 12.dp)
                        )
                        HorizontalDivider(modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // GOOGLE SIGN IN BUTTON
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .border(1.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
                            .clickable(enabled = !isAuthenticating) {
                                triggerGoogleSignIn()
                            }
                            .padding(vertical = 14.dp, horizontal = 16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AccountCircle,
                                contentDescription = "Google Account",
                                tint = if (isAuthenticating) GrayLight else BlueAccent,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = if (isAuthenticating) "Menghubungkan Google..." else "Lanjutkan dengan Google",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // BACK BUTTON IF ACCESSED FROM SETTINGS
                    if (onBackClick != null) {
                        Tactile3DButton(
                            text = "BATAL / KEMBALI",
                            onClick = onBackClick,
                            type = TactileButtonType.SECONDARY
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}
