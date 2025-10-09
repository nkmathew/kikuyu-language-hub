const {getDefaultConfig, mergeConfig} = require('@react-native/metro-config');

/**
 * Metro configuration
 * https://reactnative.dev/docs/metro
 *
 * @type {import('metro-config').MetroConfig}
 */
const config = {
  resolver: {
    assetExts: ['json', 'png', 'jpg', 'jpeg', 'svg', 'gif', 'webp'],
  },
};

module.exports = mergeConfig(getDefaultConfig(__dirname), config);
