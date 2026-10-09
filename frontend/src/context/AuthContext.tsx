import {createContext,useContext,useMemo,useState,type ReactNode} from 'react';
import type {AuthResponse} from '../types';
import {api} from '../services/api';
interface AuthCtx {user:AuthResponse['user']|null;login:(email:string,password:string)=>Promise<void>;register:(email:string,password:string,name:string)=>Promise<void>;logout:()=>Promise<void>}
const AuthContext=createContext<AuthCtx|null>(null);
export function AuthProvider({children}:{children:ReactNode}){
 const [user,setUser]=useState<AuthResponse['user']|null>(()=>{const raw=localStorage.getItem('stayease_user');return raw?JSON.parse(raw):null});
 const persist=(r:AuthResponse)=>{setUser(r.user);localStorage.setItem('stayease_user',JSON.stringify(r.user));localStorage.setItem('stayease_user_id',r.user.id);localStorage.setItem('stayease_token',r.token)};
 const value=useMemo<AuthCtx>(()=>({user,
   async login(e,p){persist(await api.login({email:e,password:p}))},
   async register(e,p,n){await api.register({email:e,password:p,name:n});persist(await api.login({email:e,password:p}))},
   async logout(){try{await api.logout()}finally{setUser(null);localStorage.removeItem('stayease_user');localStorage.removeItem('stayease_user_id');localStorage.removeItem('stayease_token')}}
 }),[user]);
 return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}
export const useAuth=()=>{const c=useContext(AuthContext);if(!c)throw new Error('useAuth must be inside AuthProvider');return c};
