import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react-swc';
import path from 'path';

export default defineConfig({
  plugins: [react()],
  resolve: {
    extensions: ['.js', '.jsx', '.ts', '.tsx', '.json'],
    alias: {
      'vaul@1.1.2': 'vaul',
      'sonner@2.0.3': 'sonner',
      'recharts@2.15.2': 'recharts',
      'react-resizable-panels@2.1.7': 'react-resizable-panels',
      'react-hook-form@7.55.0': 'react-hook-form',
      'react-day-picker@8.10.1': 'react-day-picker',
      'next-themes@0.4.6': 'next-themes',
      'lucide-react@0.487.0': 'lucide-react',
      'input-otp@1.4.2': 'input-otp',
      'figma:asset/f2e0d0183a438e31fe7131ed2173548b7f21aea2.png': path.resolve(
        __dirname,
        './src/assets/f2e0d0183a438e31fe7131ed2173548b7f21aea2.png'
      ),
      'figma:asset/d52bccf7f987fc9b0bda0846c0141355d7823ae1.png': path.resolve(
        __dirname,
        './src/assets/d52bccf7f987fc9b0bda0846c0141355d7823ae1.png'
      ),
      'figma:asset/d2daa75fe2b747eea9d2585ebcd97a1b43927977.png': path.resolve(
        __dirname,
        './src/assets/d2daa75fe2b747eea9d2585ebcd97a1b43927977.png'
      ),
      'figma:asset/b58690531dd5cbb1352f8e5c713b25639e76dae9.png': path.resolve(
        __dirname,
        './src/assets/b58690531dd5cbb1352f8e5c713b25639e76dae9.png'
      ),
      'figma:asset/a9f72de3098a0ae4bfa7e3d8ea1603d83da0eac0.png': path.resolve(
        __dirname,
        './src/assets/a9f72de3098a0ae4bfa7e3d8ea1603d83da0eac0.png'
      ),
      'figma:asset/a35cbf8e782d1162ac0360ad37c72b41b56ed0c6.png': path.resolve(
        __dirname,
        './src/assets/a35cbf8e782d1162ac0360ad37c72b41b56ed0c6.png'
      ),
      'figma:asset/7cbfeb4cacad8b7ff05087e6bb2f76c3ba4d921e.png': path.resolve(
        __dirname,
        './src/assets/7cbfeb4cacad8b7ff05087e6bb2f76c3ba4d921e.png'
      ),
      'figma:asset/654e0924cc8319b8a3e662daabd00ddbff610480.png': path.resolve(
        __dirname,
        './src/assets/654e0924cc8319b8a3e662daabd00ddbff610480.png'
      ),
      'figma:asset/5eb297e5fb5eb77d91e0dc360108bf8fa1a12b20.png': path.resolve(
        __dirname,
        './src/assets/5eb297e5fb5eb77d91e0dc360108bf8fa1a12b20.png'
      ),
      'figma:asset/5893cb63018d28641d46dbb011b55f946ff36643.png': path.resolve(
        __dirname,
        './src/assets/5893cb63018d28641d46dbb011b55f946ff36643.png'
      ),
      'figma:asset/54116abb0da9577e8e87663b82466c2e36792369.png': path.resolve(
        __dirname,
        './src/assets/54116abb0da9577e8e87663b82466c2e36792369.png'
      ),
      'figma:asset/3a465bc3eceefca059097f79720cc85a3b28b734.png': path.resolve(
        __dirname,
        './src/assets/3a465bc3eceefca059097f79720cc85a3b28b734.png'
      ),
      'figma:asset/1ea006dc4cf62fda2e8ddd425d0178712455c473.png': path.resolve(
        __dirname,
        './src/assets/1ea006dc4cf62fda2e8ddd425d0178712455c473.png'
      ),
      'figma:asset/10ab81539875bd08fb11acc6c58753b6c244c1e0.png': path.resolve(
        __dirname,
        './src/assets/10ab81539875bd08fb11acc6c58753b6c244c1e0.png'
      ),
      'figma:asset/060eddf81c61ee29d1847760635f03574a9494ff.png': path.resolve(
        __dirname,
        './src/assets/060eddf81c61ee29d1847760635f03574a9494ff.png'
      ),
      'embla-carousel-react@8.6.0': 'embla-carousel-react',
      'cmdk@1.1.1': 'cmdk',
      'class-variance-authority@0.7.1': 'class-variance-authority',
      '@radix-ui/react-tooltip@1.1.8': '@radix-ui/react-tooltip',
      '@radix-ui/react-toggle@1.1.2': '@radix-ui/react-toggle',
      '@radix-ui/react-toggle-group@1.1.2': '@radix-ui/react-toggle-group',
      '@radix-ui/react-tabs@1.1.3': '@radix-ui/react-tabs',
      '@radix-ui/react-switch@1.1.3': '@radix-ui/react-switch',
      '@radix-ui/react-slot@1.1.2': '@radix-ui/react-slot',
      '@radix-ui/react-slider@1.2.3': '@radix-ui/react-slider',
      '@radix-ui/react-separator@1.1.2': '@radix-ui/react-separator',
      '@radix-ui/react-select@2.1.6': '@radix-ui/react-select',
      '@radix-ui/react-scroll-area@1.2.3': '@radix-ui/react-scroll-area',
      '@radix-ui/react-radio-group@1.2.3': '@radix-ui/react-radio-group',
      '@radix-ui/react-progress@1.1.2': '@radix-ui/react-progress',
      '@radix-ui/react-popover@1.1.6': '@radix-ui/react-popover',
      '@radix-ui/react-navigation-menu@1.2.5': '@radix-ui/react-navigation-menu',
      '@radix-ui/react-menubar@1.1.6': '@radix-ui/react-menubar',
      '@radix-ui/react-label@2.1.2': '@radix-ui/react-label',
      '@radix-ui/react-hover-card@1.1.6': '@radix-ui/react-hover-card',
      '@radix-ui/react-dropdown-menu@2.1.6': '@radix-ui/react-dropdown-menu',
      '@radix-ui/react-dialog@1.1.6': '@radix-ui/react-dialog',
      '@radix-ui/react-context-menu@2.2.6': '@radix-ui/react-context-menu',
      '@radix-ui/react-collapsible@1.1.3': '@radix-ui/react-collapsible',
      '@radix-ui/react-checkbox@1.1.4': '@radix-ui/react-checkbox',
      '@radix-ui/react-avatar@1.1.3': '@radix-ui/react-avatar',
      '@radix-ui/react-aspect-ratio@1.1.2': '@radix-ui/react-aspect-ratio',
      '@radix-ui/react-alert-dialog@1.1.6': '@radix-ui/react-alert-dialog',
      '@radix-ui/react-accordion@1.2.3': '@radix-ui/react-accordion',
      '@': path.resolve(__dirname, './src'),
    },
  },
  build: {
    target: 'esnext',
    outDir: 'build',
  },
  server: {
    port: 3000,
    open: true,
  },
});
