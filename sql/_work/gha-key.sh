#!/bin/bash
set -euo pipefail
KEY=~/.ssh/gha_deploy
if [ ! -f "$KEY" ]; then
  ssh-keygen -t ed25519 -f "$KEY" -N "" -C "github-actions-smartadmin"
fi
# ensure pubkey in authorized_keys once
PUB=$(cat "${KEY}.pub")
grep -qxF "$PUB" ~/.ssh/authorized_keys 2>/dev/null || echo "$PUB" >> ~/.ssh/authorized_keys
chmod 600 ~/.ssh/authorized_keys ~/.ssh/gha_deploy
chmod 644 ~/.ssh/gha_deploy.pub
echo "PUBKEY:"
cat ~/.ssh/gha_deploy.pub
echo "KEY_READY"