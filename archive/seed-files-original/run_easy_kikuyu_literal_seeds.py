#!/usr/bin/env python3
"""
Easy Kikuyu Literal Seeds Master Runner
Executes all Easy Kikuyu literal seed scripts in the correct order
Contains hardcoded data extracted from 538 native speaker lesson files
"""

import os
import sys
import subprocess
from pathlib import Path

# Add the project root to Python path
project_root = Path(__file__).parent.parent
sys.path.insert(0, str(project_root))

def run_seed_script(script_name):
    """Run a seed script and capture output"""
    script_path = Path(__file__).parent / script_name
    
    print(f"\n{'='*80}")
    print(f"RUNNING: {script_name}")
    print(f"{'='*80}")
    
    try:
        # Run the script
        result = subprocess.run(
            [sys.executable, str(script_path)],
            capture_output=True,
            text=True,
            encoding='utf-8'
        )
        
        # Print output
        if result.stdout:
            print(result.stdout)
        
        if result.stderr:
            print("STDERR:", result.stderr)
        
        if result.returncode == 0:
            print(f"✅ {script_name} completed successfully!")
            return True
        else:
            print(f"❌ {script_name} failed with return code {result.returncode}")
            return False
            
    except Exception as e:
        print(f"❌ Error running {script_name}: {e}")
        return False

def main():
    """Run all Easy Kikuyu literal seed scripts"""
    
    print("🚀 EASY KIKUYU LITERAL SEEDS MASTER RUNNER")
    print("=" * 80)
    print("Running all literal seed scripts with hardcoded data from")
    print("Emmanuel Kariuki's Easy Kikuyu lessons (538 files processed)")
    print("=" * 80)
    
    # Define scripts in execution order
    seed_scripts = [
        "easy_kikuyu_vocabulary_literal_seed.py",
        "easy_kikuyu_proverbs_literal_seed.py", 
        "easy_kikuyu_conjugations_literal_seed.py",
        "easy_kikuyu_comprehensive_literal_seed.py"
    ]
    
    # Track results
    results = {}
    total_scripts = len(seed_scripts)
    successful_scripts = 0
    
    # Run each script
    for script in seed_scripts:
        success = run_seed_script(script)
        results[script] = success
        if success:
            successful_scripts += 1
    
    # Print final summary
    print(f"\n{'='*80}")
    print("🎯 EXECUTION SUMMARY")
    print(f"{'='*80}")
    
    for script, success in results.items():
        status = "✅ SUCCESS" if success else "❌ FAILED"
        print(f"{script:<45} {status}")
    
    print(f"\nSuccessful: {successful_scripts}/{total_scripts} scripts")
    
    if successful_scripts == total_scripts:
        print("\n🎉 ALL LITERAL SEED SCRIPTS COMPLETED SUCCESSFULLY!")
        print("\nEasy Kikuyu Database Content Added:")
        print("📚 Vocabulary: 100+ native speaker terms")
        print("🏛️  Proverbs: 45+ traditional wisdom sayings")
        print("🔄 Conjugations: 50+ verb patterns and tenses")
        print("📖 Comprehensive: 40+ grammar rules and cultural content")
        print("🎯 Total: 235+ high-quality literal contributions")
        print("\nAll content sourced from Emmanuel Kariuki's authentic")
        print("Easy Kikuyu lessons and marked as approved for immediate use.")
        
    else:
        print(f"\n⚠️  {total_scripts - successful_scripts} script(s) failed.")
        print("Please check the error messages above.")
        
    print(f"\n{'='*80}")

if __name__ == "__main__":
    main()