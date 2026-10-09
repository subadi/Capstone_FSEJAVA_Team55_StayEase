import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';
export default defineConfig({plugins:[react()],test:{environment:'jsdom',globals:true,setupFiles:'./src/test/setup.ts',coverage:{provider:'v8',reporter:['text','html','json-summary'],thresholds:{lines:80,functions:80,branches:75,statements:80},exclude:['src/main.tsx','src/types.ts','src/mock/store.ts','src/App.tsx']}}});
