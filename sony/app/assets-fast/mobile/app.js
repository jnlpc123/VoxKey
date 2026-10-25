'use strict';
const params=new URLSearchParams(location.hash.slice(1)),token=params.get('t'),publicKey=params.get('k');
history.replaceState(null,'',location.pathname);
const form=document.getElementById('form'),status=document.getElementById('status'),save=document.getElementById('save');
function random(n){const bytes=new Uint8Array(n);crypto.getRandomValues(bytes);return Array.from(bytes,b=>String.fromCharCode(b)).join('');}
function unurl(s){return forge.util.decode64(s.replace(/-/g,'+').replace(/_/g,'/'));}
if(!token||!publicKey||!window.crypto||!crypto.getRandomValues){save.disabled=true;status.textContent='配对信息缺失，请重新扫描投影仪上的二维码。';}
form.addEventListener('submit',async event=>{event.preventDefault();save.disabled=true;status.textContent='正在加密并保存…';try{
 const apiKey=document.getElementById('key').value.trim(),workspace=document.getElementById('workspace').value.trim();
 if(apiKey&&!/^[\x21-\x7e]{1,512}$/.test(apiKey))throw Error('请检查 API Key，不能含空格或中文。');
 if(workspace&&!/^[A-Za-z0-9_-]{1,128}$/.test(workspace))throw Error('请检查 Workspace ID。');
 const key=random(32),iv=random(12),cipher=forge.cipher.createCipher('AES-GCM',key);
 cipher.start({iv,additionalData:forge.util.encodeUtf8(token),tagLength:128});cipher.update(forge.util.createBuffer(forge.util.encodeUtf8(JSON.stringify({workspace,apiKey,cloud:document.getElementById('cloud').checked}))));if(!cipher.finish())throw Error('加密失败');
 const rsa=forge.pki.publicKeyFromAsn1(forge.asn1.fromDer(unurl(publicKey)));
 const wrapped=rsa.encrypt(key,'RSA-OAEP',{md:forge.md.sha256.create(),mgf1:{md:forge.md.sha256.create()}});
 const response=await fetch('/save',{method:'POST',credentials:'omit',cache:'no-store',headers:{'Content-Type':'application/json','X-Pair-Token':token},body:JSON.stringify({wrappedKey:forge.util.encode64(wrapped),iv:forge.util.encode64(iv),ciphertext:forge.util.encode64(cipher.output.getBytes()),tag:forge.util.encode64(cipher.mode.tag.getBytes())})});
 if(!response.ok)throw Error('保存失败。请检查密钥、配对有效期和 Wi-Fi，然后重试。');
 document.getElementById('key').value='';form.hidden=true;status.textContent='已保存到投影仪，二维码已失效。可以关闭此页面。';
 }catch(error){status.textContent=error.message||'连接失败，请重新扫码。';save.disabled=false;}});
