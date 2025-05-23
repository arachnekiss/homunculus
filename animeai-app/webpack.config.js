const createExpoWebpackConfigAsync = require('@expo/webpack-config');

module.exports = async function(env, argv) {
  const config = await createExpoWebpackConfigAsync(
    {
      ...env,
      // Passing true will enable the default Expo web platform
      web: {
        // Use traditional HTML and CSS
        bundler: 'webpack',
      },
    },
    argv
  );
  
  // Fix for some dependencies
  config.resolve.alias = {
    ...config.resolve.alias,
    'react-native$': 'react-native-web',
  };
  
  // Fix for crypto module
  config.resolve.fallback = {
    ...config.resolve.fallback,
    crypto: require.resolve('crypto-browserify'),
    stream: require.resolve('stream-browserify'),
  };
  
  return config;
};