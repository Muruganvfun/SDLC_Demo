# Lineaje CLI (veecli) Setup

## Download

1. Login to https://app.veedna.com
2. Navigate to: **Integrations** > **Configure Scanners** > **Download CLI**
3. Download `veecli.tar.gz`
4. Extract to this directory:
   ```bash
   tar -xzf veecli.tar.gz -C tools/lineaje/
   ```

## First-Time Registration

1. Go to Lineaje portal > Integrations > Configure Scanners
2. Click "Verify Device" > "Verify Link"
3. Copy the device code displayed
4. Run:
   ```bash
   cd tools/lineaje
   ./veecli register --devicecode <YOUR_CODE>
   ```

## Usage

### Generate SBOM from Azure DevOps Git Repository

```bash
./veecli collect --inputfile ../../.lineaje/input.json --output ../../.lineaje/output
```

### Generate SBOM from Local Source

Create a custom input.json:
```json
{
  "project": "local-project",
  "version": "1.0.0",
  "inputtype": "local",
  "inputs": [{
    "src_info": {
      "srcpath": "/path/to/source",
      "type": "local"
    }
  }]
}
```

Then run:
```bash
./veecli collect --inputfile input.json --output output
```

## Troubleshooting

### CLI Not Found
Make sure you've run `bash pre.sh` after extraction to set permissions.

### Authentication Failed
Re-register with a new device code from the portal.

### Scan Timeout
Large projects may take time. Run in a screen/tmux session:
```bash
screen -S lineaje
./veecli collect --inputfile input.json --output output
# Ctrl+A, D to detach
```

## File Structure After Setup

```
tools/lineaje/
├── veecli           # Main executable
├── pre.sh           # Permission setup script
├── lib/             # Dependencies
└── SETUP.md         # This file
```
