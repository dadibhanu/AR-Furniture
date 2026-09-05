# Bhamane Bridal Store — Production-ready Android Source

## Main features
- Fully local Room/SQLite database
- Sales with multiple items, discount and automatic totals
- Automatic bill numbers
- Rental records with photo selection and return dates
- Persistent local data after app restart
- Date-range business reporting
- Excel (.xlsx) export for sales and rentals
- WhatsApp/share-sheet daily report
- Local-only architecture; no web server or cloud database
- No PIN lock

## Build
GitHub Actions builds the Android APK automatically after changes are pushed to `main`.

The debug APK is generated under `app/build/outputs/apk/debug/` and uploaded as a workflow artifact.