const path = require('path');
const fs = require('fs-extra');

module.exports = async function(ctx) {
    const targetLibs = path.join(ctx.opts.projectRoot, './platforms/android/app/libs');
    await fs.remove(targetLibs);
    const targetLicense = path.join(ctx.opts.projectRoot, './platforms/android/app/src/main/assets/license.bin');
    await fs.remove(targetLicense);
    console.log('^_^ remove londen plugin SDK');
};