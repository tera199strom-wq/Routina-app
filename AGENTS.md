# Project Routina - Development & Database Rules

## 1. Aturan Sinkronisasi Database & Google Sheets (MANDATORY)
Setiap kali ada perubahan, penambahan fitur, atau modifikasi yang berkaitan dengan data (skema baru, kolom baru, tabel baru di aplikasi Android maupun Supabase):
1. **Sediakan SQL Supabase Lengkap**:
   - `CREATE TABLE IF NOT EXISTS` dengan kolom yang jelas.
   - `ALTER TABLE ... ADD COLUMN IF NOT EXISTS` untuk tabel lama.
   - Row Level Security (`RLS`) & Policies.
   - Trigger otomatis dari `auth.users` ke tabel publik jika berkaitan dengan user registrasi.
2. **Sediakan Kode Google Apps Script Terbaru**:
   - Skrip yang selalu ter-update untuk Google Sheets (`Routina_Database`).
   - Memasukkan tabel atau kolom baru ke dalam daftar render horizontal (`tables`) dan kartu metrik KPI.
