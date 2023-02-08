# !/bin/bash

mkdir -p /opt/aurora-blog-system-web/pageWeb
mkdir -p /opt/aurora-blog-system-web/adminWeb
mkdir -p /opt/aurora-blog-system-web/backend
echo "正在打包adminWeb模块"
cd ../auroraAdminWeb
pnpm build-aurora
echo "正在移动adminWeb"
mv -f /opt/aurora-blog-system/auroraAdminWeb/dist /opt/aurora-blog-system-web/adminWeb