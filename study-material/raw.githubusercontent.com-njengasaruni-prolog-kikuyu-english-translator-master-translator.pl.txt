sk(X) --> npk(X,human),vkp(X).

npk(X,human) --> prok(X),nk(X,human).
npk(X,human) -->nk(X,human).

vkp(X) --> vk(helping),adjk(X).
vkp(X) -->  vk(helping),vk(_,X,_,_),advk.
vkp(X)--> vk(_,X,_,_).

nk(plural,human) --> [arimu].
nk(singular,human) --> [mwarimu].
nk(plural,human) --> [athomi].
nk(singular,human) --> [muthomi].
nk(plural) --> [mabuku].
nk(singular) --> [ibuku].
nk(plural) --> [buudi].
nk(singular) --> [buudi].

vk(human,singular,present,continous) --> [arathikirirya].
vk(human,singular,present,continous) --> [arafundithia].
vk(human,plural,present,continous) --> [marafundithia].
vk(_,_,infinitive,_) --> [guthikirirywa].
vk(human,singular,_,habitual) --> [afundithagia].
vk(human,singular,_,habitual) --> [aandikaga].
vk(human,singular,present,continous) --> [araandika].
vk(human,plural,present,continous) --> [maraandika].
vk(helping) --> [ni].

prok(singular) --> [uyu].
prok(plural) --> [aya].
prok(singular) --> [ucio].
prok(plural) --> [acio].

adjk(singular) --> [muruaru].
adjk(plural) --> [aruaru].
adjk(singular) --> [muriitu].
adjk(plural) --> [ariitu].
adjk(singular) --> [mwega].
adjk(plural) --> [eega].
adjk(singular) --> [murakaru].
adjk(plural) --> [arakaru].

advk --> [kahora].
advk --> [naihenya].
advk --> [wega].
advk --> [uru].
 
%Pronouns
tran(nii,'I').
tran(ithui,we).
tran(wee,you).
tran(inyui,you).
tran(we,him).
tran(we,her).
tran(o,they).

%Verbs
tran(gufundithia,teach).
tran(afundithagia,teaches).
tran(athikagiriria,listens).
tran(mathikagiririrya,listen).
tran(aandikaga,writes).
tran(maandikaga,write).
tran(wi,are).
tran(ni,is).
tran(ni,are).
tran(ni,were).
tran(arafundithia,teaching).
tran(marafundithia,teaching).
tran(araandika,writing).
tran(maraandika,writing).
tran(andikire,wrote).

%Nouns
tran(mwarimu,teacher).
tran(muthomi,student).
tran(arimu,teachers).
tran(athomi,students).

%Demonstrative
tran(aya,these).
tran(uyu,this).
tran(ucio,that).
tran(acio,those).

%Adjective
tran(muruaru,sick).
tran(aruaru,sick).
tran(mwega,good).
tran(eega,good).
tran(murakaru,angry).
tran(arakaru,angry).
tran(muriitu,foolish).
tran(ariitu,foolish).

%Adverbs
tran(wega,well).
tran(uru,badly).
tran(kahora,slowly).
tran(naihenya,fast).

listtran([],[]).

listtran([K|Kt],[E|Et]) :- 
	listtran(Kt,Et),
	tran(K,E).
	
s(X,human) --> np(X,human),vp(X,continous). 
s(X,human) --> np(X,human),vp(X,habitual).
s(X,human) --> np(X,human),vp(X,adjective).

np(X,Y) --> (det(X),n(X,Y)).
np(plural,human) --> n(plural,human).

vp(X,continous) --> v(X,be),v(continous).
vp(X,continous) --> v(X,be),v(continous),adv.
vp(X,adjective) --> v(X,be),adj.
vp(X,habitual) --> v(X,habitual).
vp(X,habitual) --> v(X,habitual),adv.

det(singular) --> [this].
det(plural) --> [these].
det(singular) --> [that].
det(plural) --> [those].
det(_) --> [the].
det(singular) --> [a].

n(singular, human) --> [lecturer].
n(plural,human) --> [teachers].
n(singular,human) --> [teacher].
n(plural,human) --> [students].
n(singular,human) --> [student].
n(plural,writeable) --> [books].
n(singular,writeable) --> [book].
n(plural,writeable) --> [blackboards].
n(singular,writeable) --> [blackboard].

v(singular,habitual) --> [listens].
v(plural,habitual) --> [teach].
v(singular,habitual) --> [teaches].
v(singular,habitual) --> [writes].
v(plural,be) --> [are].
v(singular,be) --> [is].
v(plural,habitual) --> [talk].
v(singular,habitual) --> [talks].
v(plural,habitual) --> [write].
v(plural,habitual) --> [listen].
v(continous) --> [writing].
v(continous) --> [teaching].

conj --> [and].
conj --> [or].
conj --> [but].

pro(subject, singular) --> [he].
pro(subject, singular) --> [she].
pro(object, singular) --> [her].
pro(object, singular) --> [him].
pro(subject,plural) --> [they].
pro(object,plural) --> [them].

adj --> [sick].
adj --> [foolish].
adj --> [good].

adv --> [well].
adv --> [badly].
adv --> [slowly].
adv --> [fast].
