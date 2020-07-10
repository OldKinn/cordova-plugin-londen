const path = require('path');
const fs = require('fs-extra');

module.exports = async function(ctx) {
    const sourceLibs = path.join(ctx.opts.plugin.dir, './src/android/Library/libs');
    const targetLibs = path.join(ctx.opts.projectRoot, './platforms/android/app/libs');
    await fs.copy(sourceLibs, targetLibs);
    const sourceJNI = path.join(ctx.opts.plugin.dir, './src/android/Library/jniLibs');
    const targetJNI = path.join(ctx.opts.projectRoot, './platforms/android/app/libs');
    await fs.copy(sourceJNI, targetJNI);
    const sourceLicense = path.join(ctx.opts.plugin.dir, './src/android/Library/license.bin');
    const targetLicense = path.join(ctx.opts.projectRoot, './platforms/android/app/src/main/assets/license.bin');
    await fs.copy(sourceLicense, targetLicense);
    console.log('^_^ londen plugin add success!')
};
