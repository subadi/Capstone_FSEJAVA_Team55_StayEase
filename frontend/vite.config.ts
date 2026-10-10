import { defineConfig } from 'vitest/config';
import react from '@vitejs/plugin-react';
export default defineConfig({plugins:[react()],test:{environment:'jsdom',globals:true,setupFiles:'./src/test/setup.ts',coverage:{provider:'v8',reporter:['text','html','json-summary'],thresholds:{lines:70,functions:70,branches:70,statements:70},exclude:['src/main.tsx','src/types.ts','src/mock/store.ts','src/App.tsx']}}});
