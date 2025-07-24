Absolutely, Vincent! Here's a comprehensive `README.md` file summarizing your AES-DUKPT simulator, covering the key concepts, setup steps, and usage. You can drop this directly into your project root:

```markdown
# 🔐 AES-DUKPT Simulator

This Kotlin-based simulator demonstrates how to perform secure PIN encryption and decryption 
using **AES-DUKPT Format 4**, compliant with [ANSI X9.24-3]. It's designed for high-assurance 
financial environments using **AES-256 + CBC mode** and supports realistic key derivation and PIN block 
handling.

---

## 📦 Features

- AES-DUKPT key management with 256-bit BDK
- Format 4 PIN block generation (fixed 16-byte block)
- CBC-mode encryption with secure IV handling
- Session key derivation (IPEK ➝ transaction key)
- Real-time PIN encryption and decryption simulation
- PIN masking for secure logging
- Error handling for validation and crypto issues

---

## 🔧 Tech Stack

- **Language**: Kotlin
- **Crypto Provider**: BouncyCastle
- **Mode**: AES/CBC/PKCS5Padding
- **Format**: ISO Format 4 PIN block (16 bytes)

---

## 🚀 How to Run

### 1. Register BouncyCastle Provider
Add this at app startup:

```kotlin
Security.addProvider(BouncyCastleProvider())
```

### 2. Run the Simulator

```kotlin
fun main() {
    val simulator = DukptSimulator()
    simulator.runSimulation(pin = "5678", pan = "4012888888881881")
}
```

Note: PAN is used for metadata only, not embedded in Format 4 PIN blocks.

---

## 🔑 Key Components

| Component           | Description                                        |
|--------------------|----------------------------------------------------|
| `generateBdk()`     | Generates a 32-byte AES-256 Base Derivation Key    |
| `generateInitialKsn()` | Provides 16-byte Key Serial Number (KSN)            |
| `DukptAES.deriveIPEK()` | Derives the IPEK from BDK and KSN using AES-CMAC |
| `AesTerminalSimulator.encryptPin()` | Encrypts Format 4 PIN block using AES-256 CBC |
| `AesHsmSimulator.decryptPin()`      | Decrypts encrypted PIN block using AES-256 CBC |

---

## 🔐 Format 4 PIN Block Structure

| Byte Index  | Content                      |
|-------------|------------------------------|
| 0           | Format ID (0x04)             |
| 1           | PIN length                   |
| 2–13        | PIN digits + `0x0F` fillers  |
| 14–15       | Random entropy bytes         |

Always returns a 16-byte block — no padding required pre-encryption.

---

## 📋 Example Output

```
Simulating AES-DUKPT PIN encryption/decryption
==============================================
PIN: ****
PAN: 401288******1881

DUKPT Format ID: 4 (AES-DUKPT)
BDK (AES-256): 00112233...FEDCBA98...
Initial KSN: FFFF9876543210E00000000000000000
IPEK (Derived using AES): A1B2C3...

Terminal encrypting PIN using AES...
Encrypted PIN block: AABBCCDDEEFF...
Current KSN: FFFF9876...
IV used for CBC: 112233445566...

HSM decrypting PIN using AES...
Decrypted PIN (unmasked): 5678
```

---

## 🧪 Testing Notes

- AES encryption uses a secure IV per transaction.
- Logs show masked and unmasked PINs for debug (can toggle).
- CBC mode ensures no ciphertext repetition.
- PAN is only used for display, not embedded in Format 4 block.

---

## 📁 Future Enhancements

- Add support for Format 1 blocks (legacy interop)
- REST API wrapper with payload validation
- Full ANSI-compliant session key derivation paths
- Key rotation and multi-terminal simulation
- JUnit test suite for transaction verification

---

## 🧙 Credits

Built with cryptographic grit and Kotlin magic by Vincent 💡
Guided by Microsoft Copilot 🧠✨

---

## 🛡️ Security Warning

This simulator is intended for **educational and testing purposes only**. Never expose real PINs, keys, or logs in production without proper HSM-backed protection and compliance checks.

```

MIT License

Copyright (c) 2025 Vincent Bevia

Permission is hereby granted, free of charge, to any person obtaining a copy of this software 
and associated documentation files (the “Software”), to deal in the Software without restriction, 
including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, 
and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, 
subject to the following conditions:

The above copyright notice and this permission notice shall be included 
in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED “AS IS”, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, 
INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR 
PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE 
FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, 
ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
